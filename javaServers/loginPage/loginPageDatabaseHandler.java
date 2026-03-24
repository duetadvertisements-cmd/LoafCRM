package javaServers.loginPage;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javaServers.models.passwordHasher;


/*
RUN COMMAND: & 'C:\Users\tanis\OneDrive\Documents\JDK 21\jdk-21.0.9\bin\java.exe' '@C:\Users\tanis\AppData\Local\Temp\cp_551qbv13lw9nab5evgpdlxo45.argfile' 'javaServers.loginPage.loginPageDatabaseHandler'
    SERVER IMPLEMENTATION:
        CHANGE PATH TO DATABASE,
        CHANGE PORT,
        ADD SQLITE DRIVER .jar
*/

public class loginPageDatabaseHandler {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/login", new PostHandler());
        server.setExecutor(null);

        System.out.println("Server started on port 8080...");
        server.start();
    }

    static class PostHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            // CORS
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                // Read request body
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody())
                );

                StringBuilder requestBody = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    requestBody.append(line);
                }

                // Parse form data
                Map<String, String> formData = new HashMap<>();

                for (String pair : requestBody.toString().split("&")) {
                    String[] keyValue = pair.split("=", 2);
                    String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                    String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                    formData.put(key, value);
                }

                String username = formData.get("username");
                String password = formData.get("password");

                System.out.println("LOGIN ATTEMPT: " + username);

                try {
                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    // ✅ ONLY get stored password
                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT PASSWORD FROM LOAF_USERS WHERE USERNAME = ?"
                    );

                    statement.setString(1, username);

                    ResultSet result = statement.executeQuery();

                    String response;

                    if (result.next()) {
                        String storedHashedPassword = result.getString("PASSWORD");

                        passwordHasher hasher = new passwordHasher();

                        // ✅ Compare properly
                        if (hasher.verifyPassword(password, storedHashedPassword)) {
                            response = "SUCCESS";
                            System.out.println("Login success");
                        } else {
                            response = "WRONG_PASSWORD";
                            System.out.println("Wrong password");
                        }

                    } else {
                        response = "USER_NOT_FOUND";
                        System.out.println("User not found");
                    }

                    exchange.sendResponseHeaders(200, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();

                    statement.close();
                    connection.close();

                } catch (Exception error) {
                    error.printStackTrace();

                    String response = "ERROR: Server issue";
                    exchange.sendResponseHeaders(500, response.length());
                    exchange.getResponseBody().write(response.getBytes());
                    exchange.getResponseBody().close();
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
}