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

public class ClockOutHandler implements HttpHandler {
    
    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCorsUtils = new CORSUtility ();
        addCorsUtils.addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {
            
        } 
        
        else {
            exchange.sendResponseHeaders(405, -1);  
        }
    }
}