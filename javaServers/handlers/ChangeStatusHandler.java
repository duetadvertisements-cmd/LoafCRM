package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtils;
import javaServers.utility.FormDataUtils;
import javaServers.utility.ResponseUtils;

public class ChangeStatusHandler implements HttpHandler {
        
        public void handle(HttpExchange exchange) throws IOException {

            CORSUtils addCorsUtils = new CORSUtils ();
            addCorsUtils.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {

                   FormDataUtils formDataParser = new FormDataUtils ();
                    Map<String, String> formData = formDataParser.parseFormData(exchange.getRequestBody());

                    String ticketID = formData.get("ticketID");
                    String toStatus = formData.get ("toStatus");

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement;


                        statement = connection.prepareStatement (
                            "UPDATE LOAF_USERS SET SUCCESSFULLY_CLOSED = CASE " +
                            "WHEN ? = 1 THEN SUCCESSFULLY_CLOSED + 1 " +
                            "WHEN ? = 2 THEN SUCCESSFULLY_CLOSED - 1 END " +
                            "WHERE USERNAME = (SELECT ASSIGNED_TO FROM LOAF_TICKETS WHERE ID = ?)"
                        );

                    statement.setString(1, toStatus);
                    statement.setString(2, toStatus);
                    statement.setString(3, ticketID);

                    statement.executeUpdate ();

                    statement = connection.prepareStatement("UPDATE LOAF_TICKETS SET STATUS = ? WHERE ID = ?");
                    statement.setString(1, toStatus);
                    statement.setString(2, ticketID);
                    statement.executeUpdate();
                        
                    String response = "SUCCESS";

                    ResponseUtils responseUtils = new ResponseUtils ();
                    responseUtils.sendResponse(exchange, 200, response);

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println(e);
                    ResponseUtils responseUtils = new ResponseUtils ();
                    responseUtils.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }