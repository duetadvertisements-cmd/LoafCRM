package javaServers;

import com.sun.net.httpserver.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.text.NumberFormat.Style;
import java.util.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javaServers.models.passwordHasher;
import javaServers.models.multiFormParser;

import javaServers.utility.FormDataUtils;
import javaServers.utility.MailUtils;
import javaServers.utility.CORSUtils;
import javaServers.utility.ResponseUtils;

import javaServers.handlers.LoginHandler;
import javaServers.handlers.*;


import java.io.*;
import java.net.Socket;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;


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