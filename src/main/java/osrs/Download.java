package osrs;

import osrs.js5.*;
import osrs.util.CRC32;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class Download {
    public static final Path BASE_PATH = Path.of(System.getProperty("user.home") + "/.rscache/osrs");

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
//        try (var js5 = TcpJs5ResourceProvider.create("oldschool285.runescape.com", 43594, 222)) {
        try (var js5 = TcpJs5ResourceProvider.create("oldschool1.runescape.com", 43594, 222)) {
//        try (var js5 = new OpenRS2Js5ResourceProvider("runescape", 659)) {
            var masterIndex = new Js5MasterIndex(Js5Util.decompress(js5.get(255, 255, true)));

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                for (var i = 0; i < masterIndex.getArchiveCount(); i++) {
                    var archive = i;
                    var archiveData = masterIndex.getArchiveData(archive);

                    if (archiveData.getCrc() == 0) {
                        delete(BASE_PATH.resolve(String.valueOf(archive)));
                        System.out.println("skipping " + archive);
                        continue;
                    }

                    scope.fork(() -> {
                        downloadArchive(js5, archive);
                        return null;
                    });
                }

                scope.join().throwIfFailed();
            }
        }

        System.out.println("done");
    }

    private static void downloadArchive(Js5ResourceProvider js5, int archive) throws IOException, InterruptedException, ExecutionException {
        var archiveIndexData = js5.get(255, archive, true);
        var archiveIndex = new Js5ArchiveIndex(Js5Util.decompress(archiveIndexData));
        write(BASE_PATH.resolve(255 + "/" + archive + ".dat"), archiveIndexData);

        var keep = new HashSet<Path>();

        for (var group : archiveIndex.groupId) {
            keep.add(BASE_PATH.resolve(archive + "/" + group + ".dat"));
        }

        if (Files.exists(BASE_PATH.resolve(String.valueOf(archive)))) {
            for (var path : Files.list(BASE_PATH.resolve(String.valueOf(archive))).toList()) {
                if (!keep.contains(path)) {
                    Files.delete(path);
                }
            }
        }

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            for (var group : archiveIndex.groupId) {
                scope.fork(() -> {
                    downloadGroup(js5, archive, group, archiveIndex);
                    return null;
                });
            }

            scope.join().throwIfFailed();
        }
    }

    private static void downloadGroup(Js5ResourceProvider js5, int archive, int group, Js5ArchiveIndex archiveIndex) throws IOException {
        var path = BASE_PATH.resolve(archive + "/" + group + ".dat");

        if (Files.exists(path) && archiveIndex.groupChecksum[group] == CRC32.crc(Files.readAllBytes(path))) {
            return;
        }

        var groupData = js5.get(archive, group, false);
        System.out.println("received " + archive + "." + group);
        write(path, groupData);
    }

    private static void write(Path path, byte[] groupData) throws IOException {
        Files.createDirectories(path.getParent());
        Files.write(path, groupData);
    }

    private static void delete(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
    }
}
