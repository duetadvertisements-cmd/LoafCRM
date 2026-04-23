package javaServers.utility;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class EmailReader {

    public static Map<String, String> readLatestEmail() throws Exception {

        String host = "imap.gmail.com";
        int port = 993;

        String email = "loafcrm@gmail.com";
        String appPassword = "ljevikvnedqqsnql";

        SSLSocket socket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(host, port);

        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        reader.readLine(); // server greeting

        // LOGIN
        writer.write("A1 LOGIN " + email + " " + appPassword + "\r\n");
        writer.flush();
        readUntilOK(reader);

        // SELECT INBOX
        writer.write("A2 SELECT INBOX\r\n");
        writer.flush();
        readUntilOK(reader);

        // FETCH latest email
        writer.write("A3 FETCH * BODY[]\r\n");
        writer.flush();

        String subject = "";
        String from = "";
        StringBuilder body = new StringBuilder();

        String line;
        boolean isBody = false;

        while ((line = reader.readLine()) != null) {

            // SUBJECT
            if (line.startsWith("Subject:")) {
                subject = line.substring(8).trim();
            }

            // FROM
            if (line.startsWith("From:")) {
                from = line.substring(5).trim();

                // extract email only
                if (from.contains("<")) {
                    from = from.substring(from.indexOf("<") + 1, from.indexOf(">"));
                }
            }

            // BODY START
            if (line.isEmpty()) {
                isBody = true;
                continue;
            }

            // BODY CONTENT
            if (isBody) {
                if (line.startsWith("--")) break; // stop at MIME boundary
                body.append(line).append("\n");
            }

            if (line.contains("A3 OK")) break;
        }

        socket.close();

        Map<String, String> result = new HashMap<>();
        result.put("title", subject);
        result.put("data", body.toString().trim());
        result.put("toEmail", from);

        return result;
    }

    private static void readUntilOK(BufferedReader reader) throws Exception {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains("OK")) break;
        }
    }
}