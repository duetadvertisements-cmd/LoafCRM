package javaServers;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.text.NumberFormat.Style;
import java.util.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import javaServers.models.passwordHasher;

public class server {

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        //SERVER CONTEXT
        server.createContext("/login", new LoginHandler());
        server.createContext("/signup", new SignUpHandler());
        server.createContext("/getRole", new roleHandler());
        server.createContext("/addTicket", new addTicketHandler());
        server.createContext("/getTickets", new getTicketsHandler());
        server.createContext("/addComment", new addCommentHandler ());
        server.createContext("/getComments", new getCommentHandler ());
        server.createContext("/getTicketsEmployee", new getTicketsEmployeeHandler ());
        //SERVER CONTEXT

        // ✅ MULTITHREADING ENABLED
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));

        System.out.println("Server started on port 8080...");
        server.start();
    }

    // 🔧 COMMON: Parse form data
    public static Map<String, String> parseFormData(InputStream body) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(body));
        StringBuilder requestBody = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }

        Map<String, String> formData = new HashMap<>();

        for (String pair : requestBody.toString().split("&")) {
            String[] keyValue = pair.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
            formData.put(key, value);
        }

        return formData;
    }

    // 🔧 COMMON: CORS
    public static void addCORS(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
    }

    // 🔧 COMMON: Send response
    public static void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.sendResponseHeaders(code, response.length());
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // =========================
    // 🔐 LOGIN HANDLER
    // =========================
    static class LoginHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = parseFormData(exchange.getRequestBody());

                    String username = formData.get("username");
                    String password = formData.get("password");

                    System.out.println("LOGIN ATTEMPT: " + username);

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT PASSWORD FROM LOAF_USERS WHERE USERNAME = ?"
                    );

                    statement.setString(1, username);

                    ResultSet result = statement.executeQuery();

                    String response;

                    if (result.next()) {
                        String storedPassword = result.getString("PASSWORD");

                        passwordHasher hasher = new passwordHasher();

                        if (hasher.verifyPassword(password, storedPassword)) {
                            response = "SUCCESS";
                        } else {
                            response = "WRONG_PASSWORD";
                        }

                    } else {
                        response = "USER_NOT_FOUND";
                    }

                    sendResponse(exchange, 200, response);

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // =========================
    // 📝 SIGNUP HANDLER
    // =========================
    static class SignUpHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = parseFormData(exchange.getRequestBody());

                    String username = formData.get("username");
                    String email = formData.get("email");
                    String password = formData.get("password");

                    System.out.println("SIGNUP: " + username);

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO LOAF_USERS (USERNAME, EMAIL, PASSWORD) VALUES (?, ?, ?)"
                    );

                    passwordHasher hasher = new passwordHasher();

                    statement.setString(1, username);
                    statement.setString(2, email);
                    statement.setString(3, hasher.hashPassword(password));

                    statement.executeUpdate();

                    sendResponse(exchange, 200, "SUCCESS");

                    statement.close();
                    connection.close();

                } catch (Exception error) {

                    String message = error.getMessage();
                    String response;

                    if (message != null && message.contains("USERNAME")) {
                        response = "ERROR: Username already exists";
                    } else if (message != null && message.contains("EMAIL")) {
                        response = "ERROR: Email already exists";
                    } else {
                        response = "ERROR: Something went wrong";
                    }

                    sendResponse(exchange, 400, response);
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }
    // =========================
    // 📝 ROLE HANDLER
    // =========================    
    static class roleHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String username = formData.get("username");

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT ROLES FROM LOAF_USERS WHERE USERNAME = ?"
                    );

                    statement.setString(1, username);

                    ResultSet result = statement.executeQuery();

                    String response;

                    if (result.next()) {
                        response = result.getString("ROLES");  // 🔥 returns role
                    } else {
                        response = "USER_NOT_FOUND";
                    }

                    server.sendResponse(exchange, 200, response);

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    server.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    //=================
    //  Add Tciket
    //=================

    static class addTicketHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String title = formData.get("title");
                    String data = formData.get("data");
                    String creator = formData.get("creator");

                   

                System.out.println ("Recieved:");
                System.out.println ("Title: "+title);
                System.out.println ("Data: "+data);
                System.out.println ("Creator: "+creator);
                
                 Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO LOAF_TICKETS (TITLE, DATA, CREATOR) VALUES (?, ?, ?)"
                    );

                    statement.setString(1, title);
                    statement.setString(2, data);
                    statement.setString(3, creator);

                    statement.executeUpdate ();

                    String response;
                    response = "SUCCESS";

                    server.sendResponse(exchange, 200, response);

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println (e);
                    server.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }   

    //=================
    //  Get Tickets
    //=================

    static class getTicketsHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String username = formData.get("username");

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_TICKETS WHERE CREATOR = ?"
                    );

                    statement.setString(1, username);

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                        responseBuilder.append("\"title\":\"").append(result.getString("TITLE")).append("\",");
                        responseBuilder.append("\"data\":\"").append(result.getString("DATA")).append("\",");
                        responseBuilder.append("\"creator\":\"").append(result.getString("CREATOR")).append("\"");
                        responseBuilder.append("},");
                    }

                    if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                        responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                    }

                    responseBuilder.append("]");

                    server.sendResponse(exchange, 200, responseBuilder.toString());

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println(e);
                    server.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }

    // =========================
    // 📝 ADD COMMENT HANDLER
    // =========================    

    static class addCommentHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String title = formData.get("title");
                    String data = formData.get("data");
                    String creator = formData.get("creator");
                    String ticket = formData.get("ticket");

                   

                System.out.println ("Recieved:");
                System.out.println ("Title: "+title);
                System.out.println ("Data: "+data);
                System.out.println ("Creator: "+creator);
                System.out.println ("ticket: "+ticket);
                
                 Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO LOAF_COMMENTS (COMMENT_TITLE, COMMENT, USERNAME, TICKET_ID) VALUES (?, ?, ?, ?)"
                    );  

                    statement.setString(1, title);
                    statement.setString(2, data);
                    statement.setString(3, creator);
                    statement.setString(4, ticket);

                    statement.executeUpdate ();

                    String response;
                    response = "SUCCESS";

                    server.sendResponse(exchange, 200, response);

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println (e);
                    server.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }

    static class getCommentHandler implements HttpHandler {
        public void handle (HttpExchange exchange) throws IOException {
            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String TICKET_ID = formData.get("ticketID");

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_COMMENTS WHERE TICKET_ID = ?"
                    );

                    statement.setString(1, TICKET_ID);

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"username\":\"").append(result.getString("USERNAME")).append("\",");
                        responseBuilder.append("\"comment\":\"").append(result.getString("COMMENT")).append("\",");
                        responseBuilder.append("\"commentTitle\":\"").append(result.getString("COMMENT_TITLE")).append("\",");
                        responseBuilder.append("\"madeOn\":\"").append(result.getString("CREATED_AT")).append("\"");
                        responseBuilder.append("},");
                    }

                    if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                        responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                    }

                    responseBuilder.append("]");

                    server.sendResponse(exchange, 200, responseBuilder.toString());

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println(e);
                    server.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }

    static class getTicketsEmployeeHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String username = formData.get("username");
                    System.out.println (username);

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_TICKETS WHERE ASSIGNED_TO = ? "
                    );

                    statement.setString(1, username);

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                        responseBuilder.append("\"title\":\"").append(result.getString("TITLE")).append("\",");
                        responseBuilder.append("\"data\":\"").append(result.getString("DATA")).append("\",");
                        responseBuilder.append("\"creator\":\"").append(result.getString("CREATOR")).append("\"");
                        responseBuilder.append("},");
                    }

                    if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                        responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                    }

                    responseBuilder.append("]");

                    server.sendResponse(exchange, 200, responseBuilder.toString());

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println(e);
                    server.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }
}

    
    

