package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtils;
import javaServers.utility.FormDataUtils;
import javaServers.utility.ResponseUtils;
import javaServers.utility.JSONUtils;

public class GetCommentHandler implements HttpHandler {
        public void handle (HttpExchange exchange) throws IOException {

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

                    String TICKET_ID = formData.get("ticketID");

                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "SELECT * FROM LOAF_COMMENTS WHERE TICKET_ID = ?"
                    );

                    statement.setString(1, TICKET_ID);

                    ResultSet result = statement.executeQuery();

                    StringBuilder responseBuilder = new StringBuilder();
                    responseBuilder.append("[");

                    JSONUtils JSONUtils = new JSONUtils ();

                    while (result.next()) {
                        responseBuilder.append("{");
                        responseBuilder.append("\"username\":\"").append(JSONUtils.escapeJson(result.getString("USERNAME"))).append("\",");
                        responseBuilder.append("\"comment\":\"").append(JSONUtils.escapeJson(result.getString("COMMENT"))).append("\",");
                        responseBuilder.append("\"commentTitle\":\"").append(JSONUtils.escapeJson(result.getString("COMMENT_TITLE"))).append("\",");
                        responseBuilder.append("\"madeOn\":\"").append(JSONUtils.escapeJson(result.getString("CREATED_AT"))).append("\",");
                        responseBuilder.append("\"filepath\":\"").append(JSONUtils.escapeJson(result.getString("FILE_PATH"))).append("\"");
                        responseBuilder.append("},");
                    }

                    if (responseBuilder.charAt(responseBuilder.length() - 1) == ',') {
                        responseBuilder.deleteCharAt(responseBuilder.length() - 1);
                    }

                    responseBuilder.append("]");

                    ResponseUtils responseUtils = new ResponseUtils ();
                    responseUtils.sendResponse(exchange, 200, responseBuilder.toString ());

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println(e);
                    ResponseUtils responseUtils = new ResponseUtils ();
                    responseUtils.sendResponse(exchange, 200, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }