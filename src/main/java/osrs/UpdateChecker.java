package osrs;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UpdateChecker {
    private static final HttpClient HTTP = HttpClient.newBuilder().build();

    public static void main(String[] args) throws IOException, InterruptedException {
        var serverVersion = getServerVersion();
        System.out.println("[Update Checker] Current version is " + serverVersion);

        if (checkServerAccepts(serverVersion + 1, 1)) {
            System.out.println("[Update Checker] Server accepts " + (serverVersion + 1) + ", client update next week");
        } else {
            System.out.println("[Update Checker] No client update next week");
        }
    }

    private static int getServerVersion() throws IOException, InterruptedException {
        var response = HTTP.send(HttpRequest.newBuilder(URI.create("https://oldschool1.runescape.com/jav_config.ws")).build(), HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("unexpected http response: " + response.statusCode());
        }

        for (var line : response.body().lines().toList()) {
            if (line.startsWith("param=25=")) {
                return Integer.parseInt(line.split("=")[2]);
            }
        }

        throw new IOException("received jav_config does not contain current server version");
    }

    private static boolean checkServerAccepts(int version, int subversion) throws IOException {
        try (var socket = new Socket("oldschool180.runescape.com", 43594)) {
            socket.getOutputStream().write(16);
            socket.getOutputStream().write(0); // length >> 8
            socket.getOutputStream().write(8); // length >> 0
            socket.getOutputStream().write(version >> 24);
            socket.getOutputStream().write(version >> 16);
            socket.getOutputStream().write(version >> 8);
            socket.getOutputStream().write(version >> 0);
            socket.getOutputStream().write(subversion >> 24);
            socket.getOutputStream().write(subversion >> 16);
            socket.getOutputStream().write(subversion >> 8);
            socket.getOutputStream().write(subversion >> 0);
            socket.getOutputStream().flush();

            var reply = socket.getInputStream().read();

            return switch (reply) {
                case -1 -> true;
                case 6 -> false;
                default -> throw new IllegalStateException("unknown reply: " + reply);
            };
        }
    }
}
