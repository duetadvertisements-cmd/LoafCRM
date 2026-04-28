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
import javaServers.utility.JSONUtility;
import javaServers.utility.ResponseUtilty;

public class GetTicketsHandler implements HttpHandler {
    String DATABASE_URL = "jdbc:sqlite:C:\\\\Users\\\\tanis\\\\OneDrive\\\\Documents\\\\LoafCRM\\\\resources\\\\LOAFCRM.db";

    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCORSUtility = new CORSUtility ();
        addCORSUtility.addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {

            try {

                FormDataUtility formDataParser = new FormDataUtility ();
                Map<String, String> formData = formDataParser.parseFormData(exchange.getRequestBody());

                String username = formData.get("username");

                Connection databaseConnection = DriverManager.getConnection (DATABASE_URL);

                PreparedStatement selectStatement;


                //THIS IS TO CHECK IF THE USER IS ADMIN, THE ADMIN SENDS THIS KEY AND IF IT MATCHES THEN IT RETURNS ALL TICKETS
                if (username.equals("4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe")) {
                    selectStatement = databaseConnection.prepareStatement(
                        "SELECT * FROM LOAF_TICKETS"
                    );
                }
                else {
                    selectStatement = databaseConnection.prepareStatement(
                        "SELECT * FROM LOAF_TICKETS WHERE TRIM (CREATOR) = ?"
                    );

                    selectStatement.setString(1, username);
                }
                

                ResultSet selectStatementResult = selectStatement.executeQuery();

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("[");

                JSONUtility JSONUtility = new JSONUtility ();

                while (selectStatementResult.next()) {
                    responseBuilder.append("{");
                    responseBuilder.append("\"id\":").append(selectStatementResult.getInt("ID")).append(",");
                    responseBuilder.append("\"title\":\"").append(JSONUtility.escapeJson(selectStatementResult.getString("TITLE"))).append("\",");
                    responseBuilder.append("\"data\":\"").append(JSONUtility.escapeJson(selectStatementResult.getString("DATA"))).append("\",");
                    responseBuilder.append("\"status\":\"").append(JSONUtility.escapeJson(selectStatementResult.getString("STATUS"))).append("\",");
                    responseBuilder.append("\"creator\":\"").append(JSONUtility.escapeJson(selectStatementResult.getString("CREATOR"))).append("\",");
                    responseBuilder.append("\"madeBy\":\"").append(JSONUtility.escapeJson(selectStatementResult.getString("MADE_BY"))).append("\"");
                    responseBuilder.append("},");
                }

                if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                    responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                }

                responseBuilder.append("]");

                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 200, responseBuilder.toString ());

                selectStatement.close();
                databaseConnection.close();

            } catch (Exception e) {
                System.err.println(e);
                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}