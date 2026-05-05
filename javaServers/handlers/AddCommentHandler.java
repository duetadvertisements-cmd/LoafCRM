package javaServers.handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.models.multiFormParser;
import javaServers.utility.CORSUtility;
import javaServers.utility.MailUtilty;
import javaServers.utility.ResponseUtilty;


public class AddCommentHandler implements HttpHandler {
    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    
    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCorsUtils = new CORSUtility ();
        addCorsUtils.addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        //MAKE CONNECTION HERE FOR GLOBAL USAGE
        Connection connection = null;


        if ("POST".equals(exchange.getRequestMethod())) {
            
            
            try {
                multiFormParser parser = new multiFormParser (exchange);
                String title = parser.getField("title");
                String data = parser.getField("data");
                String creator = parser.getField("creator");
                String ticket = parser.getField("ticket");
                String toEmail = parser.getField ("toEmail");
                int isEmployeeOnly = Integer.parseInt (parser.getField ("isEmployeeOnly"));
                String filePath = null;

                if (parser.hasFile()) {
                    String fileName = System.currentTimeMillis() + "_" + parser.getFileName();
                    File saveDir = new File("uploads");
                    if (!saveDir.exists()) saveDir.mkdirs();

                    File savedFile = new File(saveDir, fileName);

                    FileOutputStream fos = new FileOutputStream(savedFile);
                    fos.write(parser.getFileData());
                    fos.close();

                    filePath = "uploads/" + fileName;
                }

                

                System.out.println ("Recieved:");
                System.out.println ("Title: "+title);
                System.out.println ("Data: "+data);
                System.out.println ("Creator: "+creator);
                System.out.println ("ticket: "+ticket);
                System.out.println ("filepath: "+filePath);


                connection = DriverManager.getConnection (DATABASE_URL);

                

                connection.setAutoCommit (false);

                PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO LOAF_COMMENTS (COMMENT_TITLE, COMMENT, USERNAME, TICKET_ID, FILE_PATH, IS_EMPLOYEE_ONLY) VALUES (?, ?, ?, ?, ?, ?)"
                );  

                

                statement.setString(1, title);
                statement.setString(2, data);
                statement.setString(3, creator);
                statement.setString(4, ticket);
                statement.setString(5, filePath);
                statement.setInt(6, isEmployeeOnly);

                statement.executeUpdate ();
                
                connection.commit ();

                String response;
                response = "SUCCESS";

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 200, response);

                //SEND MAIL

                Statement getIdStatement = connection.createStatement ();
                ResultSet recentAddedTicket = getIdStatement.executeQuery("SELECT last_insert_rowid ()");

                int ticketID = -1;
                if (recentAddedTicket.next ()) {
                    ticketID = recentAddedTicket.getInt (1);
                }

                System.out.println ("Comment Created With ID: "+ticketID);

                String emailBody = "Dear User,\n\n"
                + "This is to inform you that a comment has been successfully created based on your request.\n\n"
                + "Comment Details:\n"
                + "Comment ID: " + ticketID + "\n"
                + "Subject: " + title + "\n"
                + "Our support team will review your request and get back to you shortly.\n\n"
                + "If you have any additional information to provide, please visit our official website to manage your ticket.\n\n"
                + "Thank you for contacting us.\n\n"
                + "Best regards,\n"
                + "Support Team\n"
                + "LoafCRM";

                MailUtilty mailUtils = new MailUtilty ();
                mailUtils.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "Comment Creation With ID "+ticketID, emailBody);

                

                statement.close();
                connection.close();

            } catch (Exception e) {

                try {connection.rollback ();}
                catch (Exception ex) {System.out.println ("Problem occured with rollback");}

                System.err.println (e);
                
                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}