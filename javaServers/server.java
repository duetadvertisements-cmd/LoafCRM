package javaServers;

import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;

import javaServers.handlers.LoginHandler;
import javaServers.handlers.SignUpHandler;
import javaServers.handlers.VerifyOTPHandler;
import javaServers.handlers.RoleHandler;
import javaServers.handlers.AddTicketHandler;
import javaServers.handlers.GetTicketsHandler;
import javaServers.handlers.AddCommentHandler;
import javaServers.handlers.AddCustomerHandler;
import javaServers.handlers.AddExpectedFinishingTime;
import javaServers.handlers.AddSubTicketCommentHandler;
import javaServers.handlers.AddSubTicketHandler;
import javaServers.handlers.GetCommentHandler;
import javaServers.handlers.GetCustomerHandler;
import javaServers.handlers.GetTicketsEmployeeHandler;
import javaServers.handlers.FileServerHandler;
import javaServers.handlers.GetAmountOfTimeEmployeeHasWorked;
import javaServers.handlers.GetAmountOfTimeEmployeeHasWorkedOnDate;
import javaServers.handlers.GetAmountOfTimeEmployeeHasWorkedOnTicket;
import javaServers.handlers.GetEmployeesHandler;
import javaServers.handlers.GetExpectedEmployeeHoursRemaining;
import javaServers.handlers.GetOnlyNonCustomersHandler;
import javaServers.handlers.GetSubTicketsHandler;
import javaServers.handlers.GetUsersHandler;
import javaServers.handlers.LogClockInTicket;
import javaServers.handlers.LogClockOutTicket;
import javaServers.handlers.PromoteUserToWorkerHandler;
import javaServers.handlers.ChangeStatusHandler;
import javaServers.handlers.CheckIfUserIsCustomerHandler;
import javaServers.handlers.GetUnassignedTicketsHandler;
import javaServers.handlers.AssignTicketHandler;
import javaServers.handlers.GetEmailHandler;
import javaServers.handlers.ClockInHandler;
import javaServers.handlers.ClockOutHandler;
import javaServers.handlers.GetUserID;
import javaServers.handlers.GenerateOTPHandler;
import javaServers.handlers.GetSubTicketCommentsHandler;

import javaServers.utility.MailReader;

public class server {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        //SERVER CONTEXT
        server.createContext ("/login", new LoginHandler ());
        server.createContext ("/signup", new SignUpHandler ());
        server.createContext ("/getRole", new RoleHandler ());
        server.createContext ("/addTicket", new AddTicketHandler ());
        server.createContext ("/getTickets", new GetTicketsHandler());
        server.createContext ("/addComment", new AddCommentHandler ());
        server.createContext ("/getComments", new GetCommentHandler ());
        server.createContext ("/getTicketsEmployee", new GetTicketsEmployeeHandler ());
        server.createContext ("/uploads", new FileServerHandler ());
        server.createContext ("/getEmployees", new GetEmployeesHandler ());
        server.createContext ("/getUsers", new GetUsersHandler ());
        server.createContext ("/promoteUserToWorker", new PromoteUserToWorkerHandler ());
        server.createContext ("/changeStatus", new ChangeStatusHandler ());
        server.createContext ("/getUnassignedTickets", new GetUnassignedTicketsHandler ());
        server.createContext ("/assignTicket", new AssignTicketHandler ());
        server.createContext ("/getEmail", new GetEmailHandler ());
        server.createContext ("/clockIn", new ClockInHandler ());
        server.createContext ("/clockOut", new ClockOutHandler ());
        server.createContext ("/getUserID", new GetUserID ());
        server.createContext ("/clockInTicket", new LogClockInTicket ());
        server.createContext ("/clockOutTicket", new LogClockOutTicket ());
        server.createContext ("/getTimeWorked", new GetAmountOfTimeEmployeeHasWorked ());
        server.createContext ("/getTimeWorkedOnATicket", new GetAmountOfTimeEmployeeHasWorkedOnTicket ());
        server.createContext ("/addExpectedFinishingTime", new AddExpectedFinishingTime ());
        server.createContext ("/getExpectedEmployeeHoursRemaining", new GetExpectedEmployeeHoursRemaining ());
        server.createContext ("/addCustomer", new AddCustomerHandler ());
        server.createContext ("/getCustomers", new GetCustomerHandler ());
        server.createContext ("/getOnlyNonCustomers", new GetOnlyNonCustomersHandler ());
        server.createContext ("/checkIfCustomer", new CheckIfUserIsCustomerHandler ());
        server.createContext ("/generateOTP", new GenerateOTPHandler ());
        server.createContext ("/verifyOTP", new VerifyOTPHandler ());
        server.createContext ("/addSubTicket", new AddSubTicketHandler ());
        server.createContext ("/getSubTickets", new GetSubTicketsHandler ());
        server.createContext ("/getSubTicketComments", new GetSubTicketCommentsHandler ());
        server.createContext ("/addSubTicketComment", new AddSubTicketCommentHandler ());
        server.createContext ("/getAmountOfTimeEmployeeHasWorkedOnDate", new GetAmountOfTimeEmployeeHasWorkedOnDate ());
        //SERVER CONTEXT

        // ✅ MULTITHREADING ENABLED
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));

        server.start();
        System.out.println("Server started on port 8080...");
        //----------------------------------------------------------------------------------------------------
        System.out.println("Starting Mail Reader (background)...");

        Thread mailThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("[" + java.time.LocalDateTime.now() + "] Checking for new emails...");
                    
                    new MailReader().run();   // Run the mail checking task
                    
                    System.out.println("[" + java.time.LocalDateTime.now() + "] Mail check completed.");
                    
                } catch (Exception e) {
                    System.err.println("[" + java.time.LocalDateTime.now() + "] Error in Mail Reader: " + e.getMessage());
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(300000); // Check every 5 mins
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restore interrupt status
                    break;
                }
            }
        }, "Mail-Reader-Thread");

        mailThread.setDaemon(true);        // Important: So it doesn't block shutdown
        mailThread.start();

        System.out.println("✅ Mail Reader started successfully (runs every 5 mins)");
    }
}