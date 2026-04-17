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

public class LoginHandler implements HttpHandler {

    //DEFENITION
    String DATABASE_URL = "jdbc:sqlite:C:\\\\Users\\\\tanis\\\\OneDrive\\\\Documents\\\\LoafCRM\\\\resources\\\\LOAFCRM.db";


    public void handle (HttpExchange exchange) throws IOException {

        CORSUtility addCorsUtils = new CORSUtility ();
        addCorsUtils.addCORS (exchange);

        if ("OPTIONS".equals (exchange.getRequestMethod ())) {

            exchange.sendResponseHeaders (204, -1);
            return;
        }

        if ("POST".equals (exchange.getRequestMethod ())) {

            try {

                FormDataUtility formDataParser = new FormDataUtility ();
                Map<String, String> formData = formDataParser.parseFormData (exchange.getRequestBody ());
                   

                String username = formData.get ("username");
                String password = formData.get ("password");

                System.out.println ("LOGIN ATTEMPT: " + username);

                Connection databaseConnection = DriverManager.getConnection (DATABASE_URL);

                PreparedStatement selectQuery = databaseConnection.prepareStatement(
                    "SELECT PASSWORD FROM LOAF_USERS WHERE USERNAME = ?"
                );

                selectQuery.setString (1, username);

                ResultSet selectQueryResult = selectQuery.executeQuery ();

                String serverResponse;

                if (selectQueryResult.next ()) {
                    String storedPassword = selectQueryResult.getString ("PASSWORD");

                    passwordHasher passwordHasher = new passwordHasher ();

                    if (passwordHasher.verifyPassword (password, storedPassword)) {
                        serverResponse = "SUCCESS";
                    } else {
                        serverResponse = "WRONG_PASSWORD";
                    }

                } else {
                    serverResponse = "USER_NOT_FOUND";
                }

                ResponseUtilty serverResponseUtility = new ResponseUtilty ();
                serverResponseUtility.sendResponse (exchange, 200, serverResponse);

                selectQuery.close ();
                databaseConnection.close ();

            } catch (Exception e) {
                e.printStackTrace();
                ResponseUtilty serverResponseUtility = new ResponseUtilty ();
                serverResponseUtility.sendResponse (exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders (405, -1);
        }
    }
}