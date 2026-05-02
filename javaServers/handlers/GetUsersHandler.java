package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.JSONUtility;
import javaServers.utility.ResponseUtilty;

public class GetUsersHandler implements HttpHandler {
    
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

                databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);

                PreparedStatement selectStatement = databaseConnection.prepareStatement(
                    "SELECT * FROM LOAF_USERS WHERE ROLES = ?"
                );

                selectStatement.setString(1, "CLIENT");

                ResultSet selectStatementResult = selectStatement.executeQuery();

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("[");

                JSONUtility JSONUtility = new JSONUtility ();

                while (selectStatementResult.next()) {
                    responseBuilder.append("{");
                    responseBuilder.append("\"id\":").append(selectStatementResult.getInt("ID")).append(",");
                    responseBuilder.append("\"username\":\"").append(JSONUtility.escapeJson(selectStatementResult.getString("USERNAME"))).append("\",");
                    responseBuilder.append("\"email\":\"").append(JSONUtility.escapeJson(selectStatementResult.getString("EMAIL"))).append("\"");
                    responseBuilder.append("},");
                }

                if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                    responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                }

                responseBuilder.append("]");

                databaseConnection.commit ();

                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 200, responseBuilder.toString ());

                selectStatement.close();
                databaseConnection.close();

            } catch (Exception e) {
                System.err.println(e);
                try {
                    databaseConnection.rollback ();
                }
                catch (Exception exception) {}
                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}