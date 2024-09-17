package osrs.js5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FlatFileResourceProvider implements Js5ResourceProvider {
    private final Path path;

    public FlatFileResourceProvider(Path path) {
        this.path = path;
    }

    @Override
    public byte[] get(int archive, int group, boolean urgent) {
        try {
            var path = this.path.resolve(archive + "/" + group + ".dat");

            if (!Files.exists(path)) {
                return null;
            }

            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
