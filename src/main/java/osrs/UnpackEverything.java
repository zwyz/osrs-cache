package osrs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class UnpackEverything {
    private static final int START_INDEX = 0;

    public static void main(String[] args) throws IOException, InterruptedException {
        var caches = Files.readAllLines(Path.of("data/caches.txt"));
        var index = 0;

        for (var cache : caches) {
            var parts = cache.split(",");
            var build = Integer.parseInt(parts[0]);
            var name = parts[1];
            var id = Integer.parseInt(parts[2]);
            System.out.println("[Cache Unpacker] Unpacking " + name + " build " + build + " (" + (index + 1) + "/" + caches.size() + ")");

            if (index >= START_INDEX) {
                Unpack.unpackOpenRS2("unpacked/" + name, build, "runescape", id);
            }

            index++;
        }
    }
}
