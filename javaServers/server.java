package javaServers;

import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;

import javaServers.handlers.LoginHandler;
import javaServers.handlers.SignUpHandler;
import javaServers.handlers.RoleHandler;
import javaServers.handlers.AddTicketHandler;
import javaServers.handlers.GetTicketsHandler;
import javaServers.handlers.AddCommentHandler;
import javaServers.handlers.GetCommentHandler;
import javaServers.handlers.GetTicketsEmployeeHandler;
import javaServers.handlers.FileServerHandler;
import javaServers.handlers.GetEmployeesHandler;
import javaServers.handlers.GetUsersHandler;
import javaServers.handlers.PromoteUserToWorkerHandler;
import javaServers.handlers.ChangeStatusHandler;
import javaServers.handlers.GetUnassignedTicketsHandler;
import javaServers.handlers.AssignTicketHandler;
import javaServers.handlers.GetEmailHandler;

public class server {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        //SERVER CONTEXT
        server.createContext("/login", new LoginHandler());
        server.createContext("/signup", new SignUpHandler());
        server.createContext("/getRole", new RoleHandler());
        server.createContext("/addTicket", new AddTicketHandler());
        server.createContext("/getTickets", new GetTicketsHandler());
        server.createContext("/addComment", new AddCommentHandler ());
        server.createContext("/getComments", new GetCommentHandler ());
        server.createContext("/getTicketsEmployee", new GetTicketsEmployeeHandler ());
        server.createContext("/uploads", new FileServerHandler());
        server.createContext("/getEmployees", new GetEmployeesHandler());
        server.createContext("/getUsers", new GetUsersHandler());
        server.createContext("/promoteUserToWorker", new PromoteUserToWorkerHandler());
        server.createContext("/changeStatus", new ChangeStatusHandler());
        server.createContext("/getUnassignedTickets", new GetUnassignedTicketsHandler());
        server.createContext("/assignTicket", new AssignTicketHandler());
        server.createContext("/getEmail", new GetEmailHandler());
        //SERVER CONTEXT

        // ✅ MULTITHREADING ENABLED
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));

        System.out.println("Server started on port 8080...");
        server.start();
    }
}