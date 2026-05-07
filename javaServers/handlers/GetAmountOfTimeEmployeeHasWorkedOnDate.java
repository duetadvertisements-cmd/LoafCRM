package javaServers.handlers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javaServers.utility.CORSUtility;
import javaServers.utility.FormDataUtility;
import javaServers.utility.ResponseUtilty;

public class GetAmountOfTimeEmployeeHasWorkedOnDate implements HttpHandler {

    String DATABASE_URL = "jdbc:sqlite:C:\\Users\\tanis\\OneDrive\\Documents\\LoafCRM\\resources\\LOAFCRM.db";

    Connection databaseConnection = null;

    public void handle(HttpExchange exchange) throws IOException {

        CORSUtility addCORSUtility = new CORSUtility();
        addCORSUtility.addCORS(exchange);

        if ("OPTIONS".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        if ("POST".equals(exchange.getRequestMethod())) {

            try {

                // WAL MODE
                Connection conn = DriverManager.getConnection(DATABASE_URL);
                Statement stmt = conn.createStatement();
                stmt.execute("PRAGMA journal_mode=WAL;");
                stmt.execute("PRAGMA synchronous=NORMAL;");
                stmt.close();
                conn.close();

                FormDataUtility formDataParser = new FormDataUtility();
                Map<String, String> formData = formDataParser.parseFormData(exchange.getRequestBody());

                String userID = formData.get("userID");
                String selectedDate = formData.get("selectedDate");

                databaseConnection = DriverManager.getConnection(DATABASE_URL);
                databaseConnection.setAutoCommit(false);

                double totalHoursWorked = calculateHoursForDate(userID, selectedDate);

                databaseConnection.commit();

                ResponseUtilty responseUtility = new ResponseUtilty();
                responseUtility.sendResponse(exchange, 200, String.valueOf(totalHoursWorked));

            } catch (Exception e) {

                e.printStackTrace();

                try {
                    if (databaseConnection != null) {
                        databaseConnection.rollback();
                    }
                } catch (Exception ex) {
                }

                ResponseUtilty responseUtility = new ResponseUtilty();
                responseUtility.sendResponse(exchange, 500, "ERROR");
            } finally {
                try {
                    if (databaseConnection != null) {
                        databaseConnection.close();
                    }
                } catch (Exception ex) {
                }
            }

        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private double calculateHoursForDate(String userID, String selectedDate) throws Exception {

        LocalDate date = LocalDate.parse(selectedDate);
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = dayStart.plusDays(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String sql = """
            SELECT CLOCK_IN, CLOCK_OUT 
            FROM LOAF_ATTENDANCE 
            WHERE CLOCK_IN < ? 
              AND (CLOCK_OUT > ? OR CLOCK_OUT IS NULL) 
              AND USER_ID = ?
            ORDER BY CLOCK_IN ASC
            """;

        List<Interval> intervals = new ArrayList<>();

        try (PreparedStatement selectStatement = databaseConnection.prepareStatement(sql)) {

            selectStatement.setString(1, dayEnd.format(formatter));
            selectStatement.setString(2, dayStart.format(formatter));
            selectStatement.setString(3, userID);

            try (ResultSet resultSet = selectStatement.executeQuery()) {

                while (resultSet.next()) {

                    String clockInStr = resultSet.getString("CLOCK_IN");
                    String clockOutStr = resultSet.getString("CLOCK_OUT");

                    LocalDateTime clockIn = LocalDateTime.parse(clockInStr, formatter);
                    LocalDateTime clockOut = (clockOutStr != null) 
                            ? LocalDateTime.parse(clockOutStr, formatter) 
                            : LocalDateTime.now();

                    LocalDateTime actualStart = clockIn.isAfter(dayStart) ? clockIn : dayStart;
                    LocalDateTime actualEnd = clockOut.isBefore(dayEnd) ? clockOut : dayEnd;

                    if (actualEnd.isAfter(actualStart)) {
                        intervals.add(new Interval(actualStart, actualEnd));
                    }
                }
            }
        }

        // Merge overlapping or adjacent intervals
        if (intervals.isEmpty()) return 0.0;

        intervals.sort(Comparator.comparing(Interval::getStart));

        List<Interval> merged = new ArrayList<>();
        Interval current = intervals.get(0);

        for (int i = 1; i < intervals.size(); i++) {
            Interval next = intervals.get(i);

            if (!current.getEnd().isBefore(next.getStart())) { // overlapping or touching
                current = new Interval(current.getStart(), 
                                     current.getEnd().isAfter(next.getEnd()) ? current.getEnd() : next.getEnd());
            } else {
                merged.add(current);
                current = next;
            }
        }
        merged.add(current);

        // Calculate total hours from merged intervals
        double totalHoursWorked = 0.0;
        for (Interval iv : merged) {
            long secondsWorked = Duration.between(iv.getStart(), iv.getEnd()).getSeconds();
            totalHoursWorked += secondsWorked / 3600.0;
        }

        return totalHoursWorked;
    }

    // Helper class
    private static class Interval {
        private final LocalDateTime start;
        private final LocalDateTime end;

        public Interval(LocalDateTime start, LocalDateTime end) {
            this.start = start;
            this.end = end;
        }

        public LocalDateTime getStart() { return start; }
        public LocalDateTime getEnd() { return end; }
    }
}