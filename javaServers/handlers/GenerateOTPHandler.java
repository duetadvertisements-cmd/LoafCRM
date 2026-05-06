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
import javaServers.utility.OTPUtility;
import javaServers.utility.ResponseUtilty;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class GenerateOTPHandler implements HttpHandler {
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

                String toEmail = formData.get ("toEmail");

                LocalDateTime dateTimeNow = LocalDateTime.now(ZoneOffset.UTC);

                // ➕ Add 5 minutes
                LocalDateTime expiryDateTime = dateTimeNow.plusMinutes(5);

                // Format for SQLite
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedExpiryDateTime = expiryDateTime.format(dateTimeFormatter);

                System.out.println(formattedExpiryDateTime);
                //OTP
                int OTP = Integer.parseInt (OTPUtility.generateOTP());
                databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);

                PreparedStatement insertStatement = databaseConnection.prepareStatement(
                    "INSERT INTO LOAF_OTP_STORAGE (EXPIRATION_TIME, EMAIL, OTP) VALUES (?, ?, ?)"
                );

                insertStatement.setString (1, formattedExpiryDateTime);
                insertStatement.setString (2, toEmail);
                insertStatement.setInt (3, OTP);

                insertStatement.executeUpdate ();
                
                databaseConnection.commit ();

                // SEND MAIL

                System.out.println ("OTP Created");

                String emailBody = "Dear User,\n\n"
                    + "You have been invited by one of our users to join their team as an employee.\n\n"
                    + "To proceed with this request and complete your verification, please use the One-Time Password (OTP) provided below:\n\n"
                    + "OTP: " + OTP + "\n\n"
                    + "This OTP is valid for a limited time. Please do not share it with anyone for security reasons.\n\n"
                    + "By verifying this OTP, you will be able to continue the onboarding process and get access to the system.\n\n"
                    + "If you were not expecting this request, you may safely ignore this email.\n\n"
                    + "Best regards,\n"
                    + "Support Team\n"
                    + "LoafCRM";

                

                MailUtilty mailUtility = new MailUtilty ();
                mailUtility.sendMail ("LOAFCRM@GMAIL.COM", "ljevikvnedqqsnql", toEmail, "OTP Verification ", emailBody);

                insertStatement.close();
                databaseConnection.close();

                String response;
                response = "SUCCESS";

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 200, response);

            } catch (Exception e) {
                System.err.println (e);
                try {
                    databaseConnection.rollback ();
                }
                catch (Exception exception) {}

                ResponseUtilty responseUtils = new ResponseUtilty ();
                responseUtils.sendResponse(httpExchange, 500, "ERROR");
            }

        } else {
            httpExchange.sendResponseHeaders(405, -1);  
        }
    }
}  