package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.models.passwordHasher;
import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.ResponseUtilty;

public class SignUpHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCorsUtils = new CORSUtility ();
        addCorsUtils.addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {

            try {

                FormDataUtility formDataParser = new FormDataUtility ();
                Map<String, String> formData = formDataParser.parseFormData(exchange.getRequestBody());

                String username = formData.get("username");
                String email = formData.get("email");
                String password = formData.get("password");

                System.out.println("SIGNUP: " + username);

                Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                );


                if (!email.equalsIgnoreCase("tanishkumardewangan@outlook.com")) {
                    PreparedStatement checkStmt = connection.prepareStatement(
                        "SELECT 1 FROM LOAF_USERS WHERE EMAIL = ?"
                    );
                    checkStmt.setString(1, email);

                    ResultSet checkResult = checkStmt.executeQuery();

                    if (checkResult.next()) {
                        ResponseUtilty responseUtils = new ResponseUtilty ();
                        responseUtils.sendResponse(exchange, 400, "ERROR: Email already exists");
                        return;
                    }
                }

                
                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO LOAF_USERS (USERNAME, EMAIL, PASSWORD) VALUES (?, ?, ?)"
                );

                passwordHasher hasher = new passwordHasher();

                statement.setString(1, username);
                statement.setString(2, email);
                statement.setString(3, hasher.hashPassword(password));

                statement.executeUpdate();

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 200, "SUCCESS");

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

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 400, response);
            }

        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}