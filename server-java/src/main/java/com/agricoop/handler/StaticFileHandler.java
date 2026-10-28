package com.agricoop.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

/**
 * Serves static web frontend assets from the client directory.
 */
public class StaticFileHandler implements HttpHandler {

    private final File clientDir;

    public StaticFileHandler() {
        File dir = new File("client");
        if (!dir.exists() || !dir.isDirectory()) {
            dir = new File("../client");
        }
        this.clientDir = dir;
        System.out.println("Serving static client files from: " + this.clientDir.getAbsolutePath());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        HttpUtils.setCorsHeaders(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        URI uri = exchange.getRequestURI();
        String path = uri.getPath();

        if (path == null || path.isEmpty() || "/".equals(path)) {
            path = "/index.html";
        }

        // Prevent directory traversal attacks
        if (path.contains("..")) {
            HttpUtils.sendErrorMessage(exchange, 403, "Forbidden");
            return;
        }

        File file = new File(clientDir, path);
        if (!file.exists() || file.isDirectory()) {
            // Check if HTML extension was omitted
            File htmlCandidate = new File(clientDir, path + ".html");
            if (htmlCandidate.exists() && !htmlCandidate.isDirectory()) {
                file = htmlCandidate;
            } else {
                // SPA fallback to index.html if not an API route
                File indexFile = new File(clientDir, "index.html");
                if (indexFile.exists()) {
                    file = indexFile;
                } else {
                    HttpUtils.sendErrorMessage(exchange, 404, "File Not Found: " + path);
                    return;
                }
            }
        }

        String mimeType = getContentType(file.getName());
        exchange.getResponseHeaders().set("Content-Type", mimeType);

        long contentLength = file.length();
        exchange.sendResponseHeaders(200, contentLength);

        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = exchange.getResponseBody()) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
        }
    }

    private String getContentType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".html")) return "text/html; charset=UTF-8";
        if (lower.endsWith(".js") || lower.endsWith(".mjs")) return "application/javascript; charset=UTF-8";
        if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".ico")) return "image/x-icon";
        if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
        if (lower.endsWith(".webp")) return "image/webp";
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        if (lower.endsWith(".ttf")) return "font/ttf";
        return "application/octet-stream";
    }
}
