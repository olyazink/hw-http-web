package netology;

import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Handler;

public class Main {
    static final int PORT = 9999;
    static final int THREAD = 64;

    public static void main(String[] args) {
        ExecutorService executeIt = Executors.newFixedThreadPool(THREAD);

        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        String classicCasePath = "/classic.html";
        String pathName = "http_web/public";

        Server server = new Server(validPaths, PORT, pathName, classicCasePath);

        server.start();
        System.out.print("Connection accepted.");

        for (int i = 0; i < 64; i++) {
            executeIt.submit(new Thread(server::process));
        }
        executeIt.shutdown();

    }
}

