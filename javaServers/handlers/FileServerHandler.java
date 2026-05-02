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

        CORSUtility addCORSUtility = new CORSUtility ();
        addCORSUtility.addCORS(exchange);

        String imagePath = exchange.getRequestURI().getPath();
        File imageFile = new File(imagePath.substring(1));

        if (!imageFile.exists()) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        byte[] imageFileBytes = java.nio.file.Files.readAllBytes(imageFile.toPath());
        exchange.getResponseHeaders().add("Content-Type", getMimeType(imageFile.getName()));

        exchange.sendResponseHeaders(200, imageFileBytes.length);
        exchange.getResponseBody().write(imageFileBytes);
        exchange.getResponseBody().close();
    }

    private String getMimeType(String imagePath) {
        if (imagePath.endsWith(".png")) return "image/png";
        if (imagePath.endsWith(".jpg") || imagePath.endsWith(".jpeg")) return "image/jpeg";
        if (imagePath.endsWith(".gif")) return "image/gif";
        return "application/octet-stream";
    }
}