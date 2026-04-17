package javaServers.handlers;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.ResponseUtilty;

public class FileServerHandler implements HttpHandler {
        
    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCorsUtils = new CORSUtility ();
        addCorsUtils.addCORS(exchange);

        String path = exchange.getRequestURI().getPath();  // /uploads/xxx.png
        File file = new File(path.substring(1));

        if (!file.exists()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().add("Content-Type", getMimeType(file.getName()));

        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private String getMimeType(String name) {
        if (name.endsWith(".png")) return "image/png";
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) return "image/jpeg";
        if (name.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}