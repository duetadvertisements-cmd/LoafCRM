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

public class GetTicketsHandler implements HttpHandler {

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

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 200, responseBuilder.toString ());

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