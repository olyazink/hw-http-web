package netology;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

public class Server {

    private ServerSocket serverSocket;
    private final int port;
    private List<String> validPaths;
    private String pathName;
    private String classicCasePath;


    public Server(List<String> validPaths, int port, String pathName, String classicCasePath) {
        this.validPaths = validPaths;
        this.port = port;
        this.pathName = pathName;
        this.classicCasePath = classicCasePath;
    }

    public void start() {
        try {
            final var serverSocket = new ServerSocket(9999);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    public void process() {
        while (true) {
            try (
                    final var socket = serverSocket.accept();
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream())
            ) {
                // read only request line for simplicity
                // must be in form GET /path HTTP/1.1
                final var requestLine = in.readLine();
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    // just close socket
                    continue;
                }

                final var path = parts[1];
                if (!validPaths.contains(path)) {
                    processWrongPath(out);
                    continue;
                }

                final var filePath = Path.of(pathName, path);
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (path.equals(classicCasePath)) {
                    processClassicCase(out, filePath, mimeType);
                    continue;
                }

                processRightRequest(out, filePath, mimeType);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void processWrongPath(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    public void processRightRequest(BufferedOutputStream out, Path filePath, String mimeType)
            throws IOException {
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }

    public void processClassicCase(BufferedOutputStream out, Path filePath, String mimeType)
            throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }


}
