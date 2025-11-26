package osrs;

import osrs.util.InstancedClassLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

public class UnpackEverything {
    private static final int CONCURRENCY = 1;

    public static void main(String[] args) throws Exception {
        var caches = Files.readAllLines(Path.of("data/caches.txt"));

        try (var executor = Executors.newFixedThreadPool(CONCURRENCY)) {
            for (var cache : caches) {
                executor.submit(() -> {
                    var parts = cache.split(",");
                    var build = Integer.parseInt(parts[0]);
                    var name = parts[1];
                    var id = Integer.parseInt(parts[2]);
                    System.out.println("[Cache Unpacker] Unpacking " + name + " build " + build);
                    unpack("unpacked/" + name, build, "runescape", id);
                });
            }
        }
    }

    private static void unpack(String path, int version, String scope, int id) {
        try {
            var loader = new InstancedClassLoader(name -> name.startsWith("osrs."));
            var unpack = Class.forName("osrs.Unpack", true, loader).getMethod("unpackOpenRS2", String.class, int.class, String.class, int.class);
            unpack.invoke(null, path, version, scope, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
