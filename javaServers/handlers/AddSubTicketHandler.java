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

public class AddSubTicketHandler implements HttpHandler {
    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    Connection databaseConnection = null;

    public void handle(HttpExchange httpExchange) throws IOException {

        CORSUtility addCORSUtility = new CORSUtility ();
        addCORSUtility.addCORS (httpExchange);

        if ("OPTIONS".equals (httpExchange.getRequestMethod())) {
            httpExchange.sendResponseHeaders (204, -1);
            return;
        }

        if ("POST".equals(httpExchange.getRequestMethod ())) {

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

                System.out.println("FULL FORM DATA: " + formData);

                String title = formData.get ("title");
                String data = formData.get ("data");
                String creator = formData.get ("creator");
                String toEmail = formData.get ("toEmail");
                String madeBy = formData.get ("madeBy");
                System.out.println ("ERROR FREE");
                int parentTicket = Integer.parseInt (formData.get ("parentTicket"));

                
                System.out.println ("Recieved:");
                System.out.println ("Title: "+title);
                System.out.println ("Data: "+data);
                System.out.println ("Creator: "+creator);
                System.out.println ("Made By: "+madeBy);
                System.out.println ("Parent Ticket: "+parentTicket);
            
                databaseConnection = DriverManager.getConnection (DATABASE_URL);

                PreparedStatement insertStatement = databaseConnection.prepareStatement(
                    "INSERT INTO LOAF_SUB_TICKETS (TITLE, DATA, CREATOR, MADE_BY, PARENT_TICKET) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );

                insertStatement.setString(1, title);
                insertStatement.setString(2, data);
                insertStatement.setString(3, creator);
                insertStatement.setString(4, madeBy);
                insertStatement.setInt(5, parentTicket);

                insertStatement.executeUpdate ();
                

                // SEND MAIL

                ResultSet recentAddedTicket = insertStatement.getGeneratedKeys ();

                int ticketID = -1;
                if (recentAddedTicket.next ()) {
                    ticketID = recentAddedTicket.getInt (1);
                }

                System.out.println ("Ticket Created With ID: "+ticketID);

                String emailBody = "Dear User,\n\n"
                + "This is to inform you that a sub ticket has been successfully created based on your request.\n\n"
                + "Sub Ticket Details:\n"
                + "Sub Ticket ID: " + ticketID + "\n"
                + "Subject: " + title + "\n"
                + "Status: Open\n\n"
                + "Our support team will review your request and get back to you shortly.\n\n"
                + "If you have any additional information to provide, please visit our official website to manage your ticket.\n\n"
                + "Thank you for contacting us.\n\n"
                + "Best regards,\n"
                + "Support Team\n"
                + "LoafCRM";

                

                MailUtilty mailUtility = new MailUtilty ();
                mailUtility.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "Sub Ticket Creation With ID "+ticketID, emailBody);

                insertStatement.close();
                databaseConnection.close();

                String response;
                response = "SUCCESS";

                

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 200, response);

            } catch (Exception e) {
                System.err.println (e);

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 500, "ERROR");
            }
        } else {
            httpExchange.sendResponseHeaders(405, -1);  
        }
    }
}  