package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.ResponseUtilty;

public class LogClockInTicket implements HttpHandler {
    
    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";

    public void handle(HttpExchange httpExchange) throws IOException {

        CORSUtility addCORSUtility = new CORSUtility ();
        addCORSUtility.addCORS(httpExchange);

        if ("OPTIONS".equals(httpExchange.getRequestMethod())) {
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(httpExchange.getRequestMethod())) {
            try {
                FormDataUtility formDataParser = new FormDataUtility ();
                Map<String, String> formData = formDataParser.parseFormData(httpExchange.getRequestBody ());

                String userID = formData.get ("userID");
                String ticketID = formData.get ("ticketId");

                System.out.println ("User ID: "+userID+" has clocked In for ticket: "+ticketID);

                Connection databaseConnection = DriverManager.getConnection (DATABASE_URL);
                PreparedStatement insertStatement = databaseConnection.prepareStatement(
                    "INSERT INTO LOAF_LOG_TIME_TICKET (USER_ID, TICKET_ID) VALUES (?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );

                insertStatement.setString(1, userID);
                insertStatement.setString(2, ticketID);

                

                insertStatement.executeUpdate ();

                ResultSet insertStatementGeneratedKeys = insertStatement.getGeneratedKeys ();
                String response = "";
                if (insertStatementGeneratedKeys.next ()) {
                    response = String.valueOf (insertStatementGeneratedKeys.getInt (1));
                }
                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 200, response);

                insertStatement.close();
                databaseConnection.close();
            }
            catch (Exception error) {
                System.err.println (error);
                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 500, "ERROR");
            }
        } 
        
        else {
            httpExchange.sendResponseHeaders(405, -1);  
        }
    }
}