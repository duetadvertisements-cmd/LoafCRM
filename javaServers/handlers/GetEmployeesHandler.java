package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.ResponseUtilty;

public class GetEmployeesHandler implements HttpHandler {
    
    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCorsUtils = new CORSUtility ();
        addCorsUtils.addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {

            try {

                Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                );

                PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM LOAF_USERS WHERE ROLES = ? "
                );

                statement.setString(1, "EMPLOYEE");

                ResultSet result = statement.executeQuery();

                StringBuilder responseBuilder = new StringBuilder();
                responseBuilder.append("[");

                while (result.next()) {
                    responseBuilder.append("{");
                    responseBuilder.append("\"id\":").append(result.getInt("ID")).append(",");
                    responseBuilder.append("\"username\":\"").append(result.getString("USERNAME")).append("\",");
                    responseBuilder.append("\"sucessfullyClosed\":\"").append(result.getString("SUCCESSFULLY_CLOSED")).append("\",");
                    responseBuilder.append("\"email\":\"").append(result.getString("EMAIL")).append("\"");
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