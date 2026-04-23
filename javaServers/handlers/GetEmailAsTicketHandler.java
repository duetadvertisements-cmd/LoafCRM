package javaServers.handlers;

import com.sun.net.httpserver.*;
import java.io.IOException;
import java.util.Map;

import javaServers.utility.EmailReader;
import javaServers.utility.ResponseUtilty;
import javaServers.utility.CORSUtility;

public class GetEmailAsTicketHandler implements HttpHandler {

    public void handle(HttpExchange exchange) throws IOException {

        new CORSUtility().addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("GET".equals(exchange.getRequestMethod())) {
            try {
                Map<String, String> email = EmailReader.readLatestEmail();

                String subject = email.get("title");
                String body = email.get("data");
                String from = email.get("toEmail");

                String json = "{"
                        + "\"subject\":\"" + escape(subject) + "\","
                        + "\"body\":\"" + escape(body) + "\","
                        + "\"from\":\"" + escape(from) + "\""
                        + "}";

                new ResponseUtilty().sendResponse(exchange, 200, json);

            } catch (Exception e) {
                e.printStackTrace();
                new ResponseUtilty().sendResponse(exchange, 500, "ERROR");
            }
        }
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"").replace("\n", "\\n");
    }
}