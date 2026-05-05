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

public class GetTicketsEmployeeHandler implements HttpHandler {
    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    Connection databaseConnection = null;

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
                System.out.println (username);

                databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);

                PreparedStatement selectStatement = databaseConnection.prepareStatement(
                    "SELECT lt.*, lwf.WORKING_FOR " +
                    "FROM LOAF_TICKETS lt " +
                    "LEFT JOIN LOAF_WORKING_FOR lwf " +
                    "ON lt.CREATOR = lwf.USER " +
                    "WHERE lt.ASSIGNED_TO = ?"
                );

                selectStatement.setString(1, username);

                ResultSet selectStatementResult = selectStatement.executeQuery();

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("[");

                JSONUtility JSONUtility = new JSONUtility ();
                
                while (selectStatementResult.next()) {
                    responseBuilder.append("{");
                    responseBuilder.append("\"id\":").append(selectStatementResult.getInt("ID")).append(",");
                    responseBuilder.append("\"title\":\"").append (JSONUtility.escapeJson (selectStatementResult.getString("TITLE"))).append("\",");
                    responseBuilder.append("\"data\":\"").append(JSONUtility.escapeJson (selectStatementResult.getString("DATA"))).append("\",");
                    responseBuilder.append("\"status\":\"").append(JSONUtility.escapeJson (selectStatementResult.getString("STATUS"))).append("\",");
                    responseBuilder.append("\"creator\":\"").append(JSONUtility.escapeJson (selectStatementResult.getString ("CREATOR"))).append("\",");
                    responseBuilder.append("\"madeBy\":\"").append(JSONUtility.escapeJson (selectStatementResult.getString ("MADE_BY"))).append("\",");
                    responseBuilder.append("\"workingForCustomer\":\"").append(JSONUtility.escapeJson (selectStatementResult.getString ("WORKING_FOR"))).append("\",");
                    responseBuilder.append("\"expectedFinishingTime\":\"").append(JSONUtility.escapeJson (selectStatementResult.getString ("EXPECTED_FINISHING_TIME"))).append("\"");
                    responseBuilder.append("},");
                    System.out.println (selectStatementResult.getString ("EXPECTED_FINISHING_TIME"));
                }

                

                if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                    responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                }

                responseBuilder.append("]");

                databaseConnection.commit ();

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 200, responseBuilder.toString ());

                selectStatement.close();
                databaseConnection.close();

            } catch (Exception e) {
                System.err.println(e);
                try {
                    databaseConnection.rollback ();
                }
                catch (Exception exception) {}
                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}