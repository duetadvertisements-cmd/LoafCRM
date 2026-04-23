package javaServers;

import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;
import java.util.Map;

import javaServers.handlers.LoginHandler;
import javaServers.handlers.SignUpHandler;
import javaServers.handlers.RoleHandler;
import javaServers.handlers.AddTicketHandler;
import javaServers.handlers.GetTicketsHandler;
import javaServers.handlers.AddCommentHandler;
import javaServers.handlers.GetCommentHandler;
import javaServers.handlers.GetTicketsEmployeeHandler;
import javaServers.handlers.FileServerHandler;
import javaServers.handlers.GetAmountOfTimeEmployeeHasWorked;
import javaServers.handlers.GetAmountOfTimeEmployeeHasWorkedOnTicket;
import javaServers.handlers.GetEmployeesHandler;
import javaServers.handlers.GetUsersHandler;
import javaServers.handlers.LogClockInTicket;
import javaServers.handlers.LogClockOutTicket;
import javaServers.handlers.PromoteUserToWorkerHandler;
import javaServers.handlers.ChangeStatusHandler;
import javaServers.handlers.GetUnassignedTicketsHandler;
import javaServers.handlers.AssignTicketHandler;
import javaServers.handlers.GetEmailHandler;
import javaServers.handlers.ClockInHandler;
import javaServers.utility.EmailReader;
import javaServers.handlers.ClockOutHandler;
import javaServers.handlers.GetUserID;

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
        //SERVER CONTEXT

        // ✅ MULTITHREADING ENABLED
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));

        System.out.println("Server started on port 8080...");



        new Thread(() -> {

            String lastProcessed = "";

            while (true) {
                try {
                    System.out.println("Checking email...");

                    Map<String, String> email = EmailReader.readLatestEmail();

                    String title = email.get("title");
                    String data = email.get("data");
                    String toEmail = email.get("toEmail");

                    // skip empty
                    if (title == null || title.trim().isEmpty()) {
                        Thread.sleep(10000);
                        continue;
                    }

                    String unique = title + toEmail;

                    // avoid duplicates
                    if (!unique.equals(lastProcessed)) {
                        lastProcessed = unique;

                        AddTicketHandler.createTicket(
                            title,
                            data,
                            "EMAIL_SYSTEM",
                            toEmail,
                            "EMAIL"
                        );

                        System.out.println("AUTO ticket created: " + title);
                    }

                    Thread.sleep(10000); // check every 10 sec

                } catch (Exception e) {
                    e.printStackTrace();
                    try { Thread.sleep(5000); } catch (Exception ignored) {}
                }
            }

        }).start();




        server.start();
    }
}