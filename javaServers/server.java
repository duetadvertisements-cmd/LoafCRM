package javaServers;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.text.NumberFormat.Style;
import java.util.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javaServers.models.passwordHasher;
import javaServers.models.multiFormParser;


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
        server.createContext("/uploads", new FileServerHandler());
        server.createContext("/getEmployees", new getEmployeesHandler());
        server.createContext("/getUsers", new getUsersHandler());
        server.createContext("/promoteUserToWorker", new promoteUserToWorkerHandler());
        server.createContext("/changeStatus", new changeStatusHandler());
        server.createContext("/getUnassignedTickets", new getUnassignedTicketsHandler());
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

                    PreparedStatement statement;

                    if (username.equals("4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe")) {
                        statement = connection.prepareStatement(
                            "SELECT * FROM LOAF_TICKETS"
                        );
                    }
                    else {
                        statement = connection.prepareStatement(
                            "SELECT * FROM LOAF_TICKETS WHERE CREATOR = ?"
                        );

                        statement.setString(1, username);
                    }
                    

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                        responseBuilder.append("\"title\":\"").append(result.getString("TITLE")).append("\",");
                        responseBuilder.append("\"data\":\"").append(result.getString("DATA")).append("\",");
                        responseBuilder.append("\"status\":\"").append(result.getString("STATUS")).append("\",");
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
                    multiFormParser parser = new multiFormParser (exchange);
                    String title = parser.getField("title");
                    String data = parser.getField("data");
                    String creator = parser.getField("creator");
                    String ticket = parser.getField("ticket");
                    String filePath = null;

                    if (parser.hasFile()) {
                        String fileName = System.currentTimeMillis() + "_" + parser.getFileName();
                        File saveDir = new File("uploads");
                        if (!saveDir.exists()) saveDir.mkdirs();

                        File savedFile = new File(saveDir, fileName);

                        FileOutputStream fos = new FileOutputStream(savedFile);
                        fos.write(parser.getFileData());
                        fos.close();

                        filePath = "uploads/" + fileName;
                    }

                   

                System.out.println ("Recieved:");
                System.out.println ("Title: "+title);
                System.out.println ("Data: "+data);
                System.out.println ("Creator: "+creator);
                System.out.println ("ticket: "+ticket);
                
                 Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO LOAF_COMMENTS (COMMENT_TITLE, COMMENT, USERNAME, TICKET_ID, FILE_PATH) VALUES (?, ?, ?, ?, ?)"
                    );  

                    statement.setString(1, title);
                    statement.setString(2, data);
                    statement.setString(3, creator);
                    statement.setString(4, ticket);
                    statement.setString (5, filePath);

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
                        responseBuilder.append("\"madeOn\":\"").append(result.getString("CREATED_AT")).append("\",");
                        responseBuilder.append("\"filepath\":\"").append(result.getString("FILE_PATH")).append("\"");
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
                        responseBuilder.append("\"status\":\"").append(result.getString("STATUS")).append("\",");
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
    static class FileServerHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {
            server.addCORS(exchange);

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

    static class getEmployeesHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_USERS WHERE ROLES = ? "
                    );

                    statement.setString(1, "EMPLOYEE");

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                        responseBuilder.append("\"username\":\"").append(result.getString("USERNAME")).append("\",");
                        responseBuilder.append("\"sucessfullyClosed\":\"").append(result.getString("SUCCESSFULLY_CLOSED")).append("\",");
                        responseBuilder.append("\"email\":\"").append(result.getString("EMAIL")).append("\"");
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
    static class getUsersHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_USERS WHERE ROLES = ?"
                    );

                    statement.setString(1, "CLIENT");

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                        responseBuilder.append("\"username\":\"").append(result.getString("USERNAME")).append("\",");
                        responseBuilder.append("\"email\":\"").append(result.getString("EMAIL")).append("\"");
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

    static class promoteUserToWorkerHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {

                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String id = formData.get("id");

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "UPDATE LOAF_USERS SET ROLES = ? WHERE ID = ?"
                    );

                    statement.setString(1, "EMPLOYEE");
                    statement.setString(2, id);

                    statement.executeUpdate();



                    String response = "SUCCESS";

                    server.sendResponse(exchange, 200, response);

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
    static class changeStatusHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {

                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String id = formData.get("ticketID");
                    String toStatus = formData.get ("toStatus");

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "UPDATE LOAF_TICKETS SET STATUS = ? WHERE ID = ?"
                    );

                    statement.setString(1, toStatus);
                    statement.setString(2, id);

                    statement.executeUpdate();

                    if (Integer.parseInt (toStatus) == 1) {
                        statement = connection.prepareStatement (
                            "UPDATE LOAF_USER u JOIN LOAF_TICKETS t ON u.USERNAME = t.Assigned"
                        );
                    }

                    String response = "SUCCESS";

                    server.sendResponse(exchange, 200, response);

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

    static class getUnassignedTicketsHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            server.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {
                    Map<String, String> formData = server.parseFormData(exchange.getRequestBody());

                    String key = formData.get("key");

                    if (!"4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe".equals(key)) {
                        server.sendResponse(exchange, 403, "INVALID KEY");
                        return;
                    }

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_TICKETS WHERE ASSIGNED_TO = ? "
                    );

                    statement.setString(1, "null");

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                        responseBuilder.append("\"title\":\"").append(result.getString("TITLE")).append("\",");
                        responseBuilder.append("\"data\":\"").append(result.getString("DATA")).append("\",");
                        responseBuilder.append("\"status\":\"").append(result.getString("STATUS")).append("\",");
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

    
    

