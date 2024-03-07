package osrs.js5;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Semaphore;

public class OpenRS2Js5ResourceProvider implements Js5ResourceProvider, AutoCloseable {
    private static final HttpClient HTTP = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private static final int MAX_CONCURRENT_REQUESTS = 50;
    private final String scope;
    private final int id;
    private final Semaphore semaphore = new Semaphore(MAX_CONCURRENT_REQUESTS);

    public OpenRS2Js5ResourceProvider(String scope, int id) {
        this.scope = scope;
        this.id = id;
    }

    @Override
    public byte[] get(int archive, int group, boolean priority) {

        try {
            semaphore.acquire();
            var response = HTTP.send(HttpRequest.newBuilder(URI.create("https://archive.openrs2.org/caches/" + scope + "/" + id + "/archives/" + archive + "/groups/" + group + ".dat")).build(), HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200) {
                throw new IOException("received response " + response.statusCode() + " on archive " + archive + " group " + group);
            }

            return response.body();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        } finally {
            semaphore.release();
        }
    }

    @Override
    public void close() {

    }
}
