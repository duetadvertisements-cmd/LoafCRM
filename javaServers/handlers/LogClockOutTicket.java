package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.ResponseUtilty;

public class LogClockOutTicket implements HttpHandler {
    
    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    Connection databaseConnection = null;

    public void handle(HttpExchange httpExchange) throws IOException {

        CORSUtility addCORSUtility = new CORSUtility ();
        addCORSUtility.addCORS(httpExchange);

        if ("OPTIONS".equals(httpExchange.getRequestMethod())) {
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(httpExchange.getRequestMethod())) {
            try {
                //LOCK-TIME PREVENTION
                Connection conn = DriverManager.getConnection(DATABASE_URL);
                Statement stmt = conn.createStatement();

                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");

                stmt.close();
                conn.close();
                
                FormDataUtility formDataParser = new FormDataUtility ();
                Map<String, String> formData = formDataParser.parseFormData(httpExchange.getRequestBody ());

                String userID = formData.get ("userID");

                System.out.println ("User ID: "+userID+" has clocked Out for ticket");

                Connection databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);

                PreparedStatement updateStatement = databaseConnection.prepareStatement(
                    "UPDATE LOAF_LOG_TIME_TICKET SET LOG_OUT = CURRENT_TIMESTAMP WHERE USER_ID = ? AND LOG_OUT IS NULL"
                );

                updateStatement.setString(1, userID);

                

                updateStatement.executeUpdate ();

                String response = "SUCCESS";

                databaseConnection.commit ();

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 200, response);

                updateStatement.close();
                databaseConnection.close();
            }
            catch (Exception error) {
                System.err.println (error);
                try {
                    databaseConnection.rollback ();
                }
                catch (Exception exception) {}
                
                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 500, "ERROR");
            }
        } 
        
        else {
            httpExchange.sendResponseHeaders(405, -1);  
        }
    }
}