package osrs.js5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

public class OpenRS2Js5ResourceProvider implements Js5ResourceProvider, AutoCloseable {
    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
    private static final int MAX_CONCURRENT_REQUESTS = 50;
    private final String scope;
    private final int id;
    private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);
    private final byte[] masterIndexData;
    private final ConcurrentHashMap<Integer, byte[]> archiveIndexData;
    private final Js5MasterIndex masterIndex;
    private final ConcurrentHashMap<Integer, Js5ArchiveIndex> archiveIndex;

    public OpenRS2Js5ResourceProvider(String scope, int id) {
        this.scope = scope;
        this.id = id;

        // Load master index (needed for CRCs)
        this.masterIndexData = request(255, 255);
        this.masterIndex = new Js5MasterIndex(Js5Util.decompress(masterIndexData));

        // Initialise array for archive indices (needed for CRCs)
        this.archiveIndexData = new ConcurrentHashMap<>();
        this.archiveIndex = new ConcurrentHashMap<>();
    }

    @Override
    public byte[] get(int archive, int group, boolean urgent) {
        if (archive == 255 && group == 255) {
            return masterIndexData;
        } else if (archive == 255) {
            return archiveIndexData.computeIfAbsent(group, _ -> request(archive, group, masterIndex.getArchiveData(group).getVersion(), masterIndex.getArchiveData(group).getCrc()));
        } else {
            var index = archiveIndex.computeIfAbsent(archive, _ -> new Js5ArchiveIndex(Js5Util.decompress(get(255, archive, true))));
            return request(archive, group, index.groupVersion[group], index.groupChecksum[group]);
        }
    }

    private byte[] request(int archive, int group) {
        return request(archive, group, "https://archive.openrs2.org/caches/" + scope + "/" + id + "/archives/" + archive + "/groups/" + group + ".dat");
    }

    private byte[] request(int archive, int group, int version, int checksum) {
        return request(archive, group, "https://archive.openrs2.org/caches/" + scope + "/archives/" + archive + "/groups/" + group + "/versions/" + version + "/checksums/" + checksum + ".dat");
    }

    private byte[] request(int archive, int group, String url) {
        var failureCount = 0;

        while (true) {
            try {
                semaphore.acquire();
                System.out.println("requesting " + archive + "." + group);
                var response = HTTP.send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofByteArray());

                if (response.statusCode() != 200) {
                    throw new IOException("received response " + response.statusCode() + " on archive " + archive + " group " + group);
                }

                System.out.println("received " + archive + "." + group);
                return response.body();
            } catch (IOException e) {
                if (failureCount++ >= 5) {
                    throw new UncheckedIOException(e);
                }
            } catch (InterruptedException e) {
                throw new AssertionError(e);
            } finally {
                semaphore.release();
            }
        }
    }

    @Override
    public void close() {

    }
}
