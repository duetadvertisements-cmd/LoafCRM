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

    //DEFENATION
    String DATABASE_URL = "jdbc:sqlite:C:\\\\Users\\\\tanis\\\\OneDrive\\\\Documents\\\\LoafCRM\\\\resources\\\\LOAFCRM.db";


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

                String username = formData.get ("username");
                String email = formData.get ("email");
                String password = formData.get ("password");

                System.out.println ("SIGNUP: " + username);

                Connection databaseConnection = DriverManager.getConnection (DATABASE_URL);

                // IGNORE CASE FOR TESTING PURPOSES
                if (!email.equalsIgnoreCase ("tanishkumardewangan@outlook.com")) {
                    PreparedStatement checkStatement = databaseConnection.prepareStatement (
                        "SELECT 1 FROM LOAF_USERS WHERE EMAIL = ?"
                    );
                    checkStatement.setString (1, email);

                    ResultSet checkStatementResult = checkStatement.executeQuery ();

                    if (checkStatementResult.next()) {
                        ResponseUtilty ServerResponseUtility = new ResponseUtilty ();
                        ServerResponseUtility.sendResponse (exchange, 400, "ERROR: Email already exists");
                        return;
                    }
                }

                
                PreparedStatement insertStatement = databaseConnection.prepareStatement(
                    "INSERT INTO LOAF_USERS (USERNAME, EMAIL, PASSWORD) VALUES (?, ?, ?)"
                );

                passwordHasher hasher = new passwordHasher ();

                insertStatement.setString (1, username);
                insertStatement.setString (2, email);
                insertStatement.setString (3, hasher.hashPassword(password));

                insertStatement.executeUpdate ();

                ResponseUtilty ServerResponseUtility = new ResponseUtilty ();
                ServerResponseUtility.sendResponse (exchange, 200, "SUCCESS");

                insertStatement.close ();
                databaseConnection.close ();

            } catch (Exception error) {

                String errorMessage = error.getMessage ();
                String ServerResponse;
                
                if (errorMessage != null && errorMessage.contains ("USERNAME")) {
                    ServerResponse = "ERROR: Username already exists";
                }
                else if (errorMessage != null && errorMessage.contains ("EMAIL")) {
                    ServerResponse = "ERROR: Email already exists";
                } 
                else {
                    ServerResponse = "ERROR: Something went wrong";
                }

                ResponseUtilty ServerResponseUtility = new ResponseUtilty ();
                ServerResponseUtility.sendResponse (exchange, 400, ServerResponse);
            }

        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}