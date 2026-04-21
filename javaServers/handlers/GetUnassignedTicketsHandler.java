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

public class GetUnassignedTicketsHandler implements HttpHandler {
    
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

                String key = formData.get("key");

                //CHECKS IF KEY IS CORRECT, 
                if (!"4k60cHb2E4Li6nf3w4K4ly3UvM4BKeUe".equals(key)) {
                    ResponseUtilty responseUtility = new ResponseUtilty ();
                    responseUtility.sendResponse(exchange, 403, "INVALID KEY");
                    return;
                }

                Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                );

                PreparedStatement selectStatement = connection.prepareStatement(
                    "SELECT * FROM LOAF_TICKETS WHERE ASSIGNED_TO = ? "
                );

                selectStatement.setString(1, "null");

                ResultSet result = selectStatement.executeQuery();

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("[");

                JSONUtility JSONUtility = new JSONUtility ();

                while (result.next()) {
                    responseBuilder.append("{");
                    responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                    responseBuilder.append("\"title\":\"").append(JSONUtility.escapeJson(result.getString("TITLE"))).append("\",");
                    responseBuilder.append("\"data\":\"").append(JSONUtility.escapeJson(result.getString("DATA"))).append("\",");
                    responseBuilder.append("\"status\":\"").append(JSONUtility.escapeJson(result.getString("STATUS"))).append("\",");
                    responseBuilder.append("\"creator\":\"").append(JSONUtility.escapeJson(result.getString("CREATOR"))).append("\"");
                    responseBuilder.append("},");
                }

                if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                    responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                }

                responseBuilder.append("]");

                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 200, responseBuilder.toString ());

                selectStatement.close();
                connection.close();

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