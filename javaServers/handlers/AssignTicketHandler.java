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

                    String assignTo = formData.get("assignTo");
                    int ticketID = Integer.parseInt(formData.get ("ticketID"));
                    String toEmail = formData.get ("toEmail");


                    Connection connection = DriverManager.getConnection(
                        "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db"
                    );

                    PreparedStatement statement = connection.prepareStatement(
                        "UPDATE LOAF_TICKETS SET ASSIGNED_TO = ? WHERE ID = ?"
                    );

                    statement.setString(1, assignTo);
                    statement.setInt(2, ticketID);

                    statement.executeUpdate();


                    String response = "SUCCESS";

                    ResponseUtilty responseUtils = new ResponseUtilty ();
                    responseUtils.sendResponse(exchange, 200, response);

                    //SEND MAIL


                    String emailBody = "Dear User,\n\n"
                        + "This is to inform you that your ticket (Ticket ID: " + ticketID + ") has been successfully assigned for further processing.\n\n"
                        + "Assignment Details:\n"
                        + "Assigned To: " + assignTo + "\n"
                        + "The assigned representative will review your request and take the necessary actions to resolve it at the earliest.\n\n"
                        + "If you have any additional information to provide, please visit our official website to manage your ticket.\n\n"
                        + "Thank you for contacting us.\n\n"
                        + "Best regards,\n"
                        + "Support Team\n"
                        + "LoafCRM";

                    MailUtilty mailUtils = new MailUtilty ();
                    mailUtils.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "Ticket Assignment With ID "+ticketID, emailBody);

                    statement.close();
                    connection.close();

                } catch (Exception e) {
                    System.err.println(e);
                    ResponseUtilty responseUtils = new ResponseUtilty ();
                    responseUtils.sendResponse(exchange, 500, "ERROR");
                }

            } else {
                exchange.sendResponseHeaders(405, -1);  
            }
        }
    }
