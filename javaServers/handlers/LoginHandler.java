package javaServers.handlers;

// =========================
    // 🔐 LOGIN HANDLER
    // =========================

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.models.passwordHasher;
import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.ResponseUtilty;

public class LoginHandler implements HttpHandler {

        public void handle(HttpExchange exchange) throws IOException {

            CORSUtility addCorsUtils = new CORSUtility ();
            addCorsUtils.addCORS(exchange);

            if ("OPTIONS".equals(exchange.getRequestMethod())) {

                exchange.sendResponseHeaders(204, -1);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {

                try {

                    FormDataUtility formDataParser = new FormDataUtility ();
                    Map<String, String> formData = formDataParser.parseFormData(exchange.getRequestBody());
                    

                    String username = formData.get("username");
                    String password = formData.get("password");

                    System.out.println("LOGIN ATTEMPT: " + username);

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT PASSWORD FROM LOAF_USERS WHERE USERNAME = ?"
                    );

                    statement.setString(1, username);

                    ResultSet result = statement.executeQuery();

                    String response;

                    if (result.next()) {
                        String storedPassword = result.getString("PASSWORD");

                        passwordHasher hasher = new passwordHasher();

                        if (hasher.verifyPassword(password, storedPassword)) {
                            response = "SUCCESS";
                        } else {
                            response = "WRONG_PASSWORD";
                        }

                    } else {
                        response = "USER_NOT_FOUND";
                    }

                    ResponseUtilty responseUtils = new ResponseUtilty ();
                    responseUtils.sendResponse(exchange, 200, response);

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    ResponseUtilty responseUtils = new ResponseUtilty ();
                    responseUtils.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }