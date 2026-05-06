package javaServers.handlers;

import java.io.File;
import java.io.FileOutputStream;
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

public class VerifyOTPHandler implements HttpHandler {
    
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

                String toEmail = formData.get ("toEmail");
                int OTP = Integer.parseInt (formData.get ("OTP"));
                String working = formData.get ("working");
                String username = formData.get ("username");


                databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);
                PreparedStatement updateStatement = databaseConnection.prepareStatement(
                    "SELECT * FROM LOAF_OTP_STORAGE WHERE EXPIRATION_TIME > CURRENT_TIMESTAMP AND EMAIL = ?"
                );
                

                updateStatement.setString(1, toEmail);

                ResultSet updateResultSet = updateStatement.executeQuery ();
                
                int StoredOTP = -1;
                if (updateResultSet.next ()) {
                    StoredOTP = updateResultSet.getInt ("OTP");
                }
                

                String response;
                if (OTP == StoredOTP) {
                    response = "Success";

                    PreparedStatement insertStatement = databaseConnection.prepareStatement(
                    "INSERT INTO LOAF_WORKING_FOR (USER, WORKING_FOR) VALUES (?, ?)"
                    );
                    PreparedStatement updateStatementForOTPStorage = databaseConnection.prepareStatement(
                    "UPDATE LOAF_OTP_STORAGE SET SUCCESS_TIME = CURRENT_TIMESTAMP WHERE EMAIL = ? AND OTP = ?"
                    );

                    insertStatement.setString (1, working);
                    insertStatement.setString (2, username);
                    
                    updateStatementForOTPStorage.executeUpdate ();
                    insertStatement.executeUpdate ();
                }
                else {
                    response = "Failed";
                }

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