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

public class RoleHandler implements HttpHandler {

    //DEFENATION
    String DATABASE_URL = "jdbc:sqlite:C:\\\\Users\\\\tanis\\\\OneDrive\\\\Documents\\\\LoafCRM\\\\resources\\\\LOAFCRM.db";

    public void handle(HttpExchange httpExchange) throws IOException {

        CORSUtility addCorsUtility = new CORSUtility ();
        addCorsUtility.addCORS(httpExchange);

        if ("OPTIONS".equals(httpExchange.getRequestMethod())) {
            httpExchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(httpExchange.getRequestMethod())) {

            try {
                FormDataUtility formDataParser = new FormDataUtility ();
                Map<String, String> formData = formDataParser.parseFormData(httpExchange.getRequestBody());

                String username = formData.get("username");

                Connection databaseConnection = DriverManager.getConnection (DATABASE_URL);

                PreparedStatement selectStatement = databaseConnection.prepareStatement(
                    "SELECT ROLES FROM LOAF_USERS WHERE USERNAME = ?"
                );

                selectStatement.setString(1, username);
                
                ResultSet selectStatementResult = selectStatement.executeQuery();

                String serverResponse;

                if (selectStatementResult.next()) {
                    serverResponse = selectStatementResult.getString("ROLES");  // 🔥 returns role
                } else {
                    serverResponse = "USER_NOT_FOUND";
                }

                ResponseUtilty serverResponseUtils = new ResponseUtilty ();
                serverResponseUtils.sendResponse(httpExchange, 200, serverResponse);

                selectStatement.close();
                databaseConnection.close();

            } catch (Exception e) {
                e.printStackTrace();
                ResponseUtilty serverResponseUtils = new ResponseUtilty ();
                serverResponseUtils.sendResponse(httpExchange, 500, "ERROR");
            }

        } else {
            httpExchange.sendResponseHeaders(405, -1);
        }
    }
}