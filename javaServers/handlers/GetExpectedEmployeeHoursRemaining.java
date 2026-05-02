package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.JSONUtility;
import javaServers.utility.ResponseUtilty;

public class GetExpectedEmployeeHoursRemaining implements HttpHandler {

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

                int userID = Integer.parseInt (formData.get ("userID"));

                databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);

                PreparedStatement selectStatement;

                String sql = "SELECT * FROM LOAF_REASSIGNMENT_LOGS " +
                            "WHERE ASSIGNED_TO_ID = ?";

                selectStatement = databaseConnection.prepareStatement (sql);

                selectStatement.setInt (1, userID);

                System.out.println ("Performed GetExpectedEmployeeHoursRemaining with user ID: "+userID);

                ResultSet selectStatementResult = selectStatement.executeQuery();

                ArrayList<Integer> allTimeTicketsAssigned = new ArrayList<>();
                ArrayList<Integer> remainingTicketsAssigned = new ArrayList<>();
                int expectedHoursToCompletion = 0;
                

                while (selectStatementResult.next()) {
                    int assignedTicket = selectStatementResult.getInt ("ASSIGNED_TICKET");
                    allTimeTicketsAssigned.add (assignedTicket);
                }

                for (int ticket:allTimeTicketsAssigned) {
                    selectStatement = databaseConnection.prepareStatement (
                        "SELECT STATUS FROM LOAF_TICKETS WHERE ID = ?"
                    );
                    selectStatement.setInt (1, ticket);

                    selectStatementResult = selectStatement.executeQuery ();

                    while (selectStatementResult.next ()) {
                        int ticketStatus = selectStatementResult.getInt ("STATUS");
                        if (ticketStatus != 2) {
                            remainingTicketsAssigned.add (ticket);
                        }
                    }
                }

                for (int ticket:remainingTicketsAssigned) {
                    selectStatement = databaseConnection.prepareStatement(
                        "SELECT * FROM LOAF_REASSIGNMENT_LOGS WHERE ASSIGNED_TICKET = ?"
                    );
                    selectStatement.setInt (1, ticket);
                    selectStatementResult = selectStatement.executeQuery ();

                    while (selectStatementResult.next ()) {
                        int hours = selectStatementResult.getInt ("EXPECTED_HOURS_TO_COMPLETION");
                        expectedHoursToCompletion = expectedHoursToCompletion+hours;
                    }
                }
                



                String response = String.valueOf (expectedHoursToCompletion);

                databaseConnection.commit ();

                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 200, response);

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
