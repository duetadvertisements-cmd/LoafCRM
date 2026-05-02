package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.ResponseUtilty;

public class ChangeStatusHandler implements HttpHandler {
        
        String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
        Connection connection = null;

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

                    String ticketID = formData.get("ticketID");
                    String toStatus = formData.get ("toStatus");

                    connection = DriverManager.getConnection (DATABASE_URL);
                    connection.setAutoCommit (false);

                    PreparedStatement updateStatement;


                        updateStatement = connection.prepareStatement (
                            "UPDATE LOAF_USERS SET SUCCESSFULLY_CLOSED = CASE " +
                            "WHEN ? = 1 THEN SUCCESSFULLY_CLOSED + 1 " +
                            "WHEN ? = 2 THEN SUCCESSFULLY_CLOSED - 1 END " +
                            "WHERE USERNAME = (SELECT ASSIGNED_TO FROM LOAF_TICKETS WHERE ID = ?)"
                        );

                    updateStatement.setString(1, toStatus);
                    updateStatement.setString(2, toStatus);
                    updateStatement.setString(3, ticketID);

                    updateStatement.executeUpdate ();

                    updateStatement = connection.prepareStatement("UPDATE LOAF_TICKETS SET STATUS = ? WHERE ID = ?");
                    updateStatement.setString(1, toStatus);
                    updateStatement.setString(2, ticketID);
                    updateStatement.executeUpdate();
                        
                    String response = "SUCCESS";
                    
                    connection.commit ();

                    ResponseUtilty responseUtility = new ResponseUtilty ();
                    responseUtility.sendResponse(exchange, 200, response);

                    updateStatement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println(e);
                    try {
                        connection.rollback ();
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