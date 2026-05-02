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

public class AssignTicketHandler implements HttpHandler {
        
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

                int assignTo = Integer.parseInt (formData.get("assignTo"));
                String assignToString = formData.get ("assignToString");
                int assignedBy = Integer.parseInt (formData.get ("assignedBy"));
                int hoursAssigned = Integer.parseInt (formData.get ("hoursAssigned"));
                int ticketID = Integer.parseInt(formData.get ("ticketID"));
                String toEmail = formData.get ("toEmail");
                String assignToEmail = formData.get ("assignToEmail");


                connection = DriverManager.getConnection (DATABASE_URL);
                connection.setAutoCommit (false);

                PreparedStatement updateStatement = connection.prepareStatement(
                    "UPDATE LOAF_TICKETS SET ASSIGNED_TO = ? WHERE ID = ?"
                );

                updateStatement.setString(1, assignToString);
                updateStatement.setInt(2, ticketID);

                updateStatement.executeUpdate();

                connection.commit ();

                String serverResponse = "SUCCESS";

                

                //SEND MAIL


                String emailBody = "Dear User,\n\n"
                    + "This is to inform you that your ticket (Ticket ID: " + ticketID + ") has been successfully assigned for further processing.\n\n"
                    + "Assignment Details:\n"
                    + "Assigned To: " + assignToString + "\n"
                    + "The assigned representative will review your request and take the necessary actions to resolve it at the earliest.\n\n"
                    + "If you have any additional information to provide, please visit our official website to manage your ticket.\n\n"
                    + "Thank you for contacting us.\n\n"
                    + "Best regards,\n"
                    + "Support Team\n"
                    + "LoafCRM";

                MailUtilty mailUtils = new MailUtilty ();
                mailUtils.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "Ticket Assignment With ID "+ticketID, emailBody);

                emailBody = "Dear Employee,\n\n"
                    +"This is to inform you that you have been assigned a ticket. \n"
                    +"Ticket Details: \n"
                    +"Ticket ID: "+ticketID+"\n"
                    +"The admin in charge of assigning you this ticket expects this ticket to be completed in "+hoursAssigned+" working hours. \n"
                    +"Please talk to the admin for any information regarding this,  \n"
                    +"Best regards, \n"
                    +"LoafCRM";
                
                mailUtils.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", assignToEmail, "Ticket Assignment With ID "+ticketID, emailBody);

                updateStatement.close();
                

                System.out.println (assignedBy);
                System.out.println (assignTo);
                System.out.println (hoursAssigned);
                PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO LOAF_REASSIGNMENT_LOGS (ASSIGNED_BY_ID, ASSIGNED_TO_ID, EXPECTED_HOURS_TO_COMPLETION, ASSIGNED_TICKET) VALUES (?, ?, ?, ?)"
                );

                insertStatement.setInt(1, assignedBy);
                insertStatement.setInt (2, assignTo);
                insertStatement.setInt (3, hoursAssigned);
                insertStatement.setInt (4, ticketID);

                insertStatement.executeUpdate();

                connection.close();
                insertStatement.close();

                ResponseUtilty serverResponseUtils = new ResponseUtilty ();
                serverResponseUtils.sendResponse(exchange, 200, serverResponse);

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
