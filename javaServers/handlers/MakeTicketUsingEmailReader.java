package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javaServers.utility.MailUtilty;

public class MakeTicketUsingEmailReader {
    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    Connection databaseConnection = null;

    public void makeTicket (String title, String data, String toEmail) throws IOException {


            try {

                toEmail = toEmail.substring(toEmail.indexOf("<") + 1, toEmail.indexOf(">"));

                // String title = formData.get ("title");
                // String data = formData.get ("data");
                // String creator = formData.get ("creator");
                // String toEmail = formData.get ("toEmail");
                // String madeBy = formData.get ("madeBy");
                GetUsernameFromEmailRaw getCreator = new GetUsernameFromEmailRaw ();
                String madeBy = "EMAIL_SYSTEM";
                String creator = getCreator.getUsername (toEmail.toUpperCase ());

                System.out.println ("NO ERROR 1");

                System.out.println ("Recieved:");
                System.out.println ("Title: "+title);
                System.out.println ("Data: "+data);
                System.out.println ("Creator: "+creator);
                System.out.println ("Made By: "+madeBy);
                System.out.println ("To Email: "+toEmail);
            
                databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);

                System.out.println ("NO ERROR 2");

                PreparedStatement insertStatement = databaseConnection.prepareStatement(
                    "INSERT INTO LOAF_TICKETS (TITLE, DATA, CREATOR, MADE_BY) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
                );
                System.out.println ("NO ERROR 3");
                insertStatement.setString(1, title);
                insertStatement.setString(2, data);
                insertStatement.setString(3, creator);
                insertStatement.setString(4, madeBy);
                
                insertStatement.executeUpdate ();
                databaseConnection.commit ();
                // SEND MAIL

                ResultSet recentAddedTicket = insertStatement.getGeneratedKeys ();

                int ticketID = -1;
                if (recentAddedTicket.next ()) {
                    ticketID = recentAddedTicket.getInt (1);
                }

                System.out.println ("NO ERROR 4");

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

                

                MailUtilty mailUtility = new MailUtilty ();
                mailUtility.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "Ticket Creation With ID "+ticketID, emailBody);

                insertStatement.close();
                databaseConnection.close();
                
                System.out.println ("NO ERROR 5");

            } catch (Exception e) {
                try {
                    databaseConnection.rollback ();
                }
                catch (Exception exception) {}
                System.out.println ("ERROR");
            }

        }
}  