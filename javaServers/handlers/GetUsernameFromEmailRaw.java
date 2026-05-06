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

public class GetUsernameFromEmailRaw {

    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    Connection databaseConnection = null;

    public String getUsername (String email) throws IOException {



        try {
            //LOCK-TIME PREVENTION
            Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();

            stmt.execute("PRAGMA journal_mode=WAL;");
            stmt.execute("PRAGMA synchronous=NORMAL;");

            stmt.close();
            conn.close();

            databaseConnection = DriverManager.getConnection (DATABASE_URL);
            databaseConnection.setAutoCommit (false);

            PreparedStatement selectStatement;

            selectStatement = databaseConnection.prepareStatement(
                "SELECT * FROM LOAF_USERS WHERE UPPERCASE_EMAIL = ?"
            );

            selectStatement.setString(1, email);
            System.out.println ("Performed getUserID with username: "+email);

            ResultSet result = selectStatement.executeQuery();

            StringBuilder responseBuilder = new StringBuilder();
            responseBuilder.append("[");
            String response = "";
            if (result.next ()) {
                response = result.getString("USERNAME");
                System.out.println ("NO PROBLEM");
            }
           
            System.out.println (response);
            databaseConnection.commit ();
            
            

            selectStatement.close();
            databaseConnection.close();

            return response;

        } 
        catch (Exception e) {
            System.err.println(e);
            try {
                databaseConnection.rollback ();
            }
            catch (Exception exception) {}

            return "ERROR";
        }
        
    }
}
