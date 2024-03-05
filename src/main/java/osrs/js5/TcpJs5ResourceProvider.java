package osrs.js5;

import osrs.util.Packet;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TcpJs5ResourceProvider implements Js5ResourceProvider, AutoCloseable {
    private static final int BLOCK_SIZE = 512;
    private static final int MAX_PENDING_REQUESTS = 100;
    private final String host;
    private final int port;
    private final int revision;
    private Socket socket;
    private final Queue<GroupRequest> unsentRequests = new LinkedBlockingQueue<>();
    private final Map<ArchiveGroup, GroupRequest> responses = new ConcurrentHashMap<>();
    private boolean connected;
    private boolean shutdownRequested = false;
    private final ReentrantReadWriteLock shutdownRequestedLock = new ReentrantReadWriteLock();
    private GroupRequest response = null;
    private int responseArchive;
    private int responseGroup;

    public TcpJs5ResourceProvider(String host, int port, int revision) {
        this.host = host;
        this.port = port;
        this.revision = revision;

        Thread.ofPlatform().start(() -> {
            try {
                processRequests();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static TcpJs5ResourceProvider create(String host, int port, int revision) {
        return new TcpJs5ResourceProvider(host, port, revision);
    }

    public void processRequests() throws IOException, InterruptedException {
        ensureConnected();

        while (true) {
            while (responses.size() < MAX_PENDING_REQUESTS - 1 && !unsentRequests.isEmpty()) {
                var request = unsentRequests.poll();
                responses.put(new ArchiveGroup(request.archive, request.group), request);
                System.out.println("request " + request.archive + " " + request.group + " " + request.priority);

                if (!request.priority) {
                    sendRequestPrefetch(request.archive, request.group);
                } else {
                    sendRequestPriority(request.archive, request.group);
                }
            }

            if (socket.getInputStream().available() > 0) {
                handleResponse();
            }

            Thread.sleep(1);
        }
    }

    private void ensureConnected() throws IOException {
        if (!connected) {
            socket = new Socket(host, port);
            socket.setTcpNoDelay(true);
            socket.setReceiveBufferSize(10_000_000);

            var packet = Packet.create(1 + 4);
            packet.p1(15);
            packet.p4(revision);
            send(packet);

            var status = receive(1).g1();

            if (status != 0) {
                throw new IOException("failed to connect " + status);
            }

            connected = true;
        }
    }

    private void sendRequestPrefetch(int archive, int group) throws IOException {
        var request = Packet.create(4);
        request.clear();
        request.p1(0);
        request.p1(archive);
        request.p2(group);
        send(request);
    }

    private void sendRequestPriority(int archive, int group) throws IOException {
        var request = Packet.create(4);
        request.p1(1);
        request.p1(archive);
        request.p2(group);
        send(request);
    }

    public void handleResponse() throws IOException {
        var blockPosition = 0;

        if (response == null) {
            var packet = receive(1 + 2);
            responseArchive = packet.g1();
            responseGroup = packet.g2();
            System.out.println("receive " + responseArchive + " " + responseGroup);

            response = responses.get(new ArchiveGroup(responseArchive, responseGroup));

            if (response == null) {
                throw new IOException("received a group that wasn't asked for: archive = " + responseArchive + " group = " + responseGroup);
            }

            if (response.buffer == null) {
                var initialData = receive(1 + 4);
                var compressionType = initialData.g1();
                var compressedSize = initialData.g4s();

                response.buffer = ByteBuffer.allocate(compressedSize + (compressionType == 0 ? 5 : 9));
                response.buffer.put((byte) compressionType);
                response.buffer.putInt(compressedSize);
            }

            blockPosition += 8;
        } else {
            if (receive(1).g1() != 255) {
                throw new IOException("expected block start byte 255");
            }

            blockPosition += 1;
        }

        response.buffer.put(receive(Math.min(response.buffer.remaining(), BLOCK_SIZE - blockPosition)).arr);

        if (response.buffer.remaining() == 0) {
            responses.remove(new ArchiveGroup(responseArchive, responseGroup));
            response.future.complete(response.buffer.array());
            response = null;
            responseArchive = -1;
            responseGroup = -1;
        }
    }

    private void send(Packet request) throws IOException {
        socket.getOutputStream().write(request.arr, 0, request.pos);
        socket.getOutputStream().flush();
    }

    private Packet receive(int size) throws IOException {
        var response = socket.getInputStream().readNBytes(size);

        if (response.length < size) {
            throw new IOException("end of stream");
        }

        return new Packet(response);
    }

    @Override
    public byte[] get(int archive, int group, boolean priority) {
        var future = getAsync(archive, group, priority);

        try {
            return future.get();
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<byte[]> getAsync(int archive, int group, boolean priority) {
        shutdownRequestedLock.readLock().lock();

        try {
            if (shutdownRequested) {
                return CompletableFuture.failedFuture(new IOException("resource provider has been shutdown"));
            } else {
                var future = new CompletableFuture<byte[]>();
                unsentRequests.add(new GroupRequest(archive, group, priority, future));
                return future;
            }
        } finally {
            shutdownRequestedLock.readLock().unlock();
        }
    }

    public void close() {
        shutdownRequestedLock.writeLock().lock();

        try {
            shutdownRequested = true;
        } finally {
            shutdownRequestedLock.writeLock().unlock();
        }
    }

    private static class GroupRequest {
        private final int archive;
        private final int group;
        private final CompletableFuture<byte[]> future;
        private final boolean priority;
        private ByteBuffer buffer;

        public GroupRequest(int archive, int group, boolean priority, CompletableFuture<byte[]> future) {
            this.archive = archive;
            this.group = group;
            this.future = future;
            this.priority = priority;
        }
    }

    private record ArchiveGroup(int archive, int group) {

    }
}
