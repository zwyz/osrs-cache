package osrs.unpack;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class XteaKeyProvider {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private static final Gson GSON = new Gson();

    public static Map<Integer, int[]> load(int version) {
        var result = new LinkedHashMap<Integer, int[]>();

        try {
            var keysResponse = HTTP_CLIENT.send(
                    HttpRequest.newBuilder(URI.create("https://archive.openrs2.org/caches/runescape/" + version + "/keys.json")).build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            if (keysResponse.statusCode() != 200) {
                throw new IllegalStateException("failed to load xtea keys, status " + keysResponse.statusCode());
            }

            var keys = (List<KeyInfo>) GSON.fromJson(keysResponse.body(), TypeToken.getParameterized(List.class, KeyInfo.class).getType());

            for (var key : keys) {
                result.put(key.group, new int[]{key.key[0], key.key[1], key.key[2], key.key[3]});
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }

        return result;
    }

    private static class KeyInfo {
        public int archive;
        public int group;
        public int name_hash;
        public String name;
        public int mapsquare;
        public int[] key;
    }

    private static class CacheInfo {
        public int id;
        public String timestamp;
        public String game;
        public String environment;
    }
}
