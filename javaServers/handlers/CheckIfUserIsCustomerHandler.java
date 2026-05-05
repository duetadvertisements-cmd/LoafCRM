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
import javaServers.utility.ResponseUtilty;

public class CheckIfUserIsCustomerHandler implements HttpHandler {

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

               databaseConnection = DriverManager.getConnection (DATABASE_URL);
               databaseConnection.setAutoCommit (false);

                PreparedStatement selectStatement;

                selectStatement = databaseConnection.prepareStatement(
                    "SELECT * FROM LOAF_USERS WHERE USERNAME = ?"
                );

                selectStatement.setString(1, username);
                System.out.println ("Performed getUserID with username: "+username);

                ResultSet result = selectStatement.executeQuery();

                String response = result.getString("IS_CUSTOMER");

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
