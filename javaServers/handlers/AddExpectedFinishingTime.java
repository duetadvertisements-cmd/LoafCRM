package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.MailUtilty;
import javaServers.utility.ResponseUtilty;

public class AddExpectedFinishingTime implements HttpHandler {
        
    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    Connection connection = null;

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

                String expectedFinishingTimeYear = formData.get("expectedFinishingTimeYear");
                String expectedFinishingTimeMonth = formData.get("expectedFinishingTimeMonth");
                String expectedFinishingTimeDate = formData.get("expectedFinishingTimeDate");
                int ticketID = Integer.parseInt(formData.get ("ticketID"));
                String toEmail = formData.get ("toEmail");

                System.out.println (expectedFinishingTimeYear);
                System.out.println (expectedFinishingTimeMonth);
                System.out.println (expectedFinishingTimeDate);
                System.out.println (ticketID);
                System.out.println (toEmail);

                //YYYY-MM-DD -> YYYY-MM-DD HH:MM:SS
                String expectedFinishingTime = expectedFinishingTimeYear+""+expectedFinishingTimeMonth+""+expectedFinishingTimeDate+""+"22:00:00";
                System.out.println (expectedFinishingTime);
                connection = DriverManager.getConnection (DATABASE_URL);

                connection.setAutoCommit (false);

                PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE LOAF_TICKETS SET EXPECTED_FINISHING_TIME = ? WHERE ID = ?"
                );

                updateStatement.setString(1, expectedFinishingTime);
                updateStatement.setInt(2, ticketID);

                updateStatement.executeUpdate();

                connection.commit ();

                String serverResponse = "SUCCESS";

                ResponseUtilty serverResponseUtils = new ResponseUtilty ();
                serverResponseUtils.sendResponse(exchange, 200, serverResponse);

                //SEND MAIL


                String emailBody = "Dear User,\n\n"
                    + "This is to inform you that your ticket (Ticket ID: " + ticketID + ") is estimated to be completed by:.\n\n"
                    + "Estimation Details:\n"
                    + "Year: " + expectedFinishingTimeYear + "\n"
                    + "Month: " + expectedFinishingTimeMonth + "\n"
                    + "Date: " + expectedFinishingTimeDate + "\n"
                    + "The assigned representative will review your request and take the necessary actions to resolve it at the earliest.\n\n"
                    + "If you have any additional information to provide, please visit our official website to manage your ticket.\n\n"
                    + "Thank you for contacting us.\n\n"
                    + "Best regards,\n"
                    + "Support Team\n"
                    + "LoafCRM";

                MailUtilty mailUtils = new MailUtilty ();
                mailUtils.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "Ticket Expected Finishing With ID "+ticketID, emailBody);

                

                updateStatement.close();
                connection.close();

            } catch (Exception e) {
                System.err.println(e);
                try {
                    connection.rollback ();
                }
                catch (Exception exception) {}
                
                ResponseUtilty serverResponseUtils = new ResponseUtilty ();
                serverResponseUtils.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}
