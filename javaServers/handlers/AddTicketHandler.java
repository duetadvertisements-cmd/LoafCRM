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
import javaServers.utility.MailUtilty;
import javaServers.utility.ResponseUtilty;

public class AddTicketHandler implements HttpHandler {

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

                String title = formData.get("title");
                String data = formData.get("data");
                String creator = formData.get("creator");
                String toEmail = formData.get ("toEmail");

                

            System.out.println ("Recieved:");
            System.out.println ("Title: "+title);
            System.out.println ("Data: "+data);
            System.out.println ("Creator: "+creator);
            
                Connection connection = DriverManager.getConnection(
                    "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                );

                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO LOAF_TICKETS (TITLE, DATA, CREATOR) VALUES (?, ?, ?)"
                );

                statement.setString(1, title);
                statement.setString(2, data);
                statement.setString(3, creator);

                statement.executeUpdate ();
                String response;
                response = "SUCCESS";

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 200, response);

                // SEND MAIL

                Statement getIdStatement = connection.createStatement ();
                ResultSet recentAddedTicket = getIdStatement.executeQuery("SELECT last_insert_rowid ()");

                int ticketID = -1;
                if (recentAddedTicket.next ()) {
                    ticketID = recentAddedTicket.getInt (1);
                }

                System.out.println ("Ticket Created With ID: "+ticketID);

                String emailBody = "Dear User,\n\n"
                + "This is to inform you that a ticket has been successfully created based on your request.\n\n"
                + "Ticket Details:\n"
                + "Ticket ID: " + ticketID + "\n"
                + "Subject: " + title + "\n"
                + "Status: Open\n\n"
                + "Our support team will review your request and get back to you shortly.\n\n"
                + "If you have any additional information to provide, please visit our official website to manage your ticket.\n\n"
                + "Thank you for contacting us.\n\n"
                + "Best regards,\n"
                + "Support Team\n"
                + "LoafCRM";

                MailUtilty mailUtils = new MailUtilty ();
                mailUtils.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "Ticket Creation With ID "+ticketID, emailBody);

                statement.close();
                connection.close();

            } catch (Exception e) {
                System.err.println (e);
                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}  