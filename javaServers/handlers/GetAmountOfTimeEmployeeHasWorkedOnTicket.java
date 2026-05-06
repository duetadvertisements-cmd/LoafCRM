package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.JSONUtility;
import javaServers.utility.ResponseUtilty;

public class GetAmountOfTimeEmployeeHasWorkedOnTicket implements HttpHandler {

    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";
    Connection databaseConnection = null;

    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCORSUtility = new CORSUtility ();
        addCORSUtility.addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {

            try {
                //LOCK-TIME PREVENTION
                Connection conn = DriverManager.getConnection(DATABASE_URL);
                Statement stmt = conn.createStatement();

                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");

                stmt.close();
                conn.close();
                
                FormDataUtility formDataParser = new FormDataUtility ();
                Map<String, String> formData = formDataParser.parseFormData(exchange.getRequestBody());

                String userID = formData.get("userID");
                String workingYear = formData.get ("workingYear");
                String workingMonth = formData.get ("workingMonth");
                String ticketID = formData.get ("ticketID");
                //THIS CAN BE CHANGED INTO GETTING FROM FORMDATA IF EACH EMPLOYEE HAS DIFFERNT PAYOUT DAYS, BUT THIS IS BETTER AS ALL EMPLOYEES HAVE
                //SAME PAYOUT DAY
                String fromWhichDay = "01";

                String fromDate = workingYear+"-"+workingMonth+"-"+fromWhichDay+" 00:00:00";

                databaseConnection = DriverManager.getConnection (DATABASE_URL);
                databaseConnection.setAutoCommit (false);

                PreparedStatement selectStatement;

                String sql = "SELECT * FROM LOAF_LOG_TIME_TICKET " +
                            "WHERE LOG_IN BETWEEN ? AND CURRENT_TIMESTAMP " +
                            "AND USER_ID = ? AND TICKET_ID = ?";

                    selectStatement = databaseConnection.prepareStatement (sql);

                    selectStatement.setString(1, fromDate);
                    selectStatement.setString(2, userID);
                    selectStatement.setString(3, ticketID);
                    System.out.println ("Performed GetAmountOfTimeEmployeeHasWorkedOnTicket with user ID: "+userID);

                ResultSet selectStatementResult = selectStatement.executeQuery();



                Double totalHoursWorked = 0.0;

                while (selectStatementResult.next()) {  
                    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                    String clockIn = selectStatementResult.getString("LOG_IN");
                    String clockOut = selectStatementResult.getString("LOG_OUT");

                    LocalDateTime clockInInDate = LocalDateTime.parse(clockIn, dateTimeFormatter);
                    LocalDateTime clockOutInDate;

                    if (clockOut == null) {
                        clockOutInDate = LocalDateTime.now(); // still working
                    } 
                    else {
                        clockOutInDate = LocalDateTime.parse(clockOut, dateTimeFormatter);
                    }

                    long secondsWorked = Duration.between(clockInInDate, clockOutInDate).getSeconds();
                    Double hoursWorked = secondsWorked / 3600.0;

                    totalHoursWorked = totalHoursWorked + hoursWorked;

                    databaseConnection.commit ();
                }


                String response = totalHoursWorked.toString ();


                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 200, response);

                selectStatement.close();
                databaseConnection.close();

            } catch (Exception e) {
                System.err.println(e);
                try {
                    databaseConnection.rollback ();
                }
                catch (Exception exception) {}
                ResponseUtilty responseUtility = new ResponseUtilty ();
                responseUtility.sendResponse(exchange, 500, "ERROR");
            }

        } else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}
