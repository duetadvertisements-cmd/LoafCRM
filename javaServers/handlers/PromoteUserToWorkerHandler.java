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

public class PromoteUserToWorkerHandler implements HttpHandler {
    
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

                String id = formData.get("id");

                Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                );

                PreparedStatement statement = connection.prepareStatement(
                    "UPDATE LOAF_USERS SET ROLES = ? WHERE ID = ?"
                );

                statement.setString(1, "EMPLOYEE");
                statement.setString(2, id);

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