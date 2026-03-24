    package javaServers.signUpPage;


    import com.sun.net.httpserver.HttpServer;
    import com.sun.net.httpserver.HttpExchange;
    import com.sun.net.httpserver.HttpHandler;
    import java.io.*;
    import java.net.InetSocketAddress;
    import java.sql.Connection;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.util.HashMap;
    import java.util.Map;
    import java.net.URLDecoder;
    import java.nio.charset.StandardCharsets;
    import javaServers.models.passwordHasher;



    /*
    RUN COMMAND: & 'C:\Users\tanis\OneDrive\Documents\JDK 21\jdk-21.0.9\bin\java.exe' '@C:\Users\tanis\AppData\Local\Temp\cp_551qbv13lw9nab5evgpdlxo45.argfile' 'javaServers.signUpPage.signUpPageDatabaseHandler' 
        SERVER IMPLEMENTATION:
            CHANGE PATH TO DATABASE,
            CHANGE PORT,
            ADD SQLITE DRIVER .jar

    */

    public class signUpPageDatabaseHandler {

        String response;

        public static void main(String[] args) throws Exception {

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            server.createContext("/signup", new PostHandler());
            server.setExecutor(null);

            System.out.println("Server started on port 8080...");
            server.start();
        }

        static class PostHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                // Allow CORS
                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, OPTIONS");
                exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

                if ("OPTIONS".equals(exchange.getRequestMethod())) {
                    exchange.sendResponseHeaders(204, -1);
                    return;
                }

                if ("POST".equals(exchange.getRequestMethod())) {

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(exchange.getRequestBody())
                    );

                    StringBuilder requestBody = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        requestBody.append(line);
                    }

                    

                    String body = requestBody.toString();

                    Map<String, String> formData = new HashMap<>();

                    for (String pair : body.split("&")) {
                        String[] keyValue = pair.split("=", 2);
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        formData.put(key, value);
                    }

                    String username = formData.get("username");
                    String email = formData.get("email");
                    String password = formData.get("password");

                    System.out.println ("DATA RECIEVED: USERNAME: "+username+"\nEMAIL: "+email+"\nPASSWORD: "+password);
    //-------FUNCTIONALITY-GOES-HERE----------------FUNCTIONALITY-GOES-HERE----------------FUNCTIONALITY-GOES-HERE----------------FUNCTIONALITY-GOES-HERE---------
                    try {
                        Class.forName("org.sqlite.JDBC");
                        Connection connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db");
                        PreparedStatement statement = connection.prepareStatement("INSERT INTO LOAF_USERS (USERNAME, EMAIL, PASSWORD) VALUES(?, ?, ?)");

                        //ADD REQUEST INITIALIZER

                        statement.setString(1, username);
                        statement.setString(2, email);
    //HASH PASSWORD HERE
                        passwordHasher passwordHasher = new passwordHasher ();
                        String hashedPassword = passwordHasher.hashPassword (password);
                        System.out.println (hashedPassword);
    //HASH PASSWORD HERE
                        statement.setString(3, hashedPassword);
                        statement.executeUpdate();

                        statement.close();
                        connection.close();

                        String response = "SUCCESS";
                        exchange.sendResponseHeaders(200, response.length());
                        exchange.getResponseBody().write(response.getBytes());

                        System.out.println("Data inserted successfully");
                    }
                    catch (Exception error) {
                        System.err.println (error);

                        String message = error.getMessage();

                        String response;

                        if (message.contains("USERNAME")) {
                            response = "ERROR: Username already exists";
                        } else if (message.contains("EMAIL")) {
                            response = "ERROR: Email already exists";
                        } else {
                            response = "ERROR: Something went wrong";
                        }

                        exchange.sendResponseHeaders(400, response.length());
                        exchange.getResponseBody().write(response.getBytes());
                    }
    //-------FUNCTIONALITY-GOES-HERE----------------FUNCTIONALITY-GOES-HERE----------------FUNCTIONALITY-GOES-HERE----------------FUNCTIONALITY-GOES-HERE---------
                } else {
                    exchange.sendResponseHeaders(405, -1);
                }
            }
        }
    }