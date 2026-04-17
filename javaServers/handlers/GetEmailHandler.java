package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.ResponseUtilty;

public class GetEmailHandler implements HttpHandler {

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

                Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                );

                PreparedStatement statement;

                    statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_USERS WHERE USERNAME = ?"
                    );

                    statement.setString(1, username);
                    System.out.println ("Performed getEmail with username: "+username);

                ResultSet result = statement.executeQuery();

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("[");

                    String response = result.getString("EMAIL");


                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 200, response);

                statement.close();
                connection.close();

            } catch (Exception e) {
                System.err.println(e);
                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}
