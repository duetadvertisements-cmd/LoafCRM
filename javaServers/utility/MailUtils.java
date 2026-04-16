package javaServers.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Base64;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class MailUtils {
    public static void sendMail(String user, String pass, String to, String subject, String body) throws Exception {

    // Helper functions inside method (using anonymous classes workaround)
        class SMTP {
            void send(BufferedWriter out, String s) throws Exception {
                out.write(s + "\r\n");
                out.flush();
                System.out.println("C: " + s);
            }

            void read(BufferedReader in) throws Exception {
                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("S: " + line);
                    if (line.length() < 4 || line.charAt(3) != '-') break;
                }
            }
        }

        SMTP smtp = new SMTP();

        // Connect
        Socket socket = new Socket("smtp.gmail.com", 587);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
        BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream()));

        smtp.read(in);

        smtp.send(out, "EHLO localhost");
        smtp.read(in);

        smtp.send(out, "STARTTLS");
        smtp.read(in);

        // TLS upgrade
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket(
                socket, "smtp.gmail.com", 587, true);

        sslSocket.startHandshake();

        in = new BufferedReader(
                new InputStreamReader(sslSocket.getInputStream()));
        out = new BufferedWriter(
                new OutputStreamWriter(sslSocket.getOutputStream()));

        smtp.send(out, "EHLO localhost");
        smtp.read(in);

        // Auth
        smtp.send(out, "AUTH LOGIN");
        smtp.read(in);

        smtp.send(out, Base64.getEncoder().encodeToString(user.getBytes()));
        smtp.read(in);

        smtp.send(out, Base64.getEncoder().encodeToString(pass.getBytes()));
        smtp.read(in);

        // Mail
        smtp.send(out, "MAIL FROM:<" + user + ">");
        smtp.read(in);

        smtp.send(out, "RCPT TO:<" + to + ">");
        smtp.read(in);

        smtp.send(out, "DATA");
        smtp.read(in);

        // Email content
        smtp.send(out, "Subject: " + subject);
        smtp.send(out, "From: " + user);
        smtp.send(out, "To: " + to);
        smtp.send(out, "");
        smtp.send(out, body);
        smtp.send(out, ".");
        smtp.read(in);

        smtp.send(out, "QUIT");
        smtp.read(in);

        sslSocket.close();

        System.out.println("Mail sent.");
    }
}
