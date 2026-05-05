package javaServers.utility;

import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import javaServers.handlers.MakeTicketUsingEmailReader;
import org.jsoup.Jsoup;
import java.util.Properties;

public class MailReader implements Runnable {

    public void run() {
        Store store = null;
        Folder inbox = null;

        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");
            props.put("mail.imaps.ssl.enable", "true");
            props.put("mail.imaps.partialfetch", "false");   // Important for large emails

            Session session = Session.getInstance(props);
            store = session.getStore("imaps");

            store.connect("imap.gmail.com", "loafcrm@gmail.com", "ljevikvnedqqsnql");

            inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            // Search for unread messages
            Message[] unread = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            System.out.println("Found " + unread.length + " unread email(s)");

            for (Message msg : unread) {
                try {
                    String subject = msg.getSubject() != null ? msg.getSubject() : "(No Subject)";
                    String sender = msg.getFrom() != null ? msg.getFrom()[0].toString() : "Unknown";

                    System.out.println("Processing: " + subject + " | From: " + sender);

                    String rawContent = getTextFromMessage(msg);
                    String cleanContent = Jsoup.parse(rawContent).text().trim();

                    // === Create Ticket ===
                    MakeTicketUsingEmailReader ticketCreator = new MakeTicketUsingEmailReader();
                    ticketCreator.makeTicket(subject, cleanContent, sender);

                    // Mark as read ONLY after successful processing
                    msg.setFlag(Flags.Flag.SEEN, true);
                    System.out.println("✓ Email processed and marked as read.");

                } catch (Exception e) {
                    System.err.println("Failed to process one email: " + e.getMessage());
                    e.printStackTrace();
                    // Do NOT mark as read if failed (so it can be retried)
                }
            }

        } catch (Exception e) {
            System.err.println("Critical MailReader Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Proper cleanup
            try { if (inbox != null) inbox.close(false); } catch (Exception ignored) {}
            try { if (store != null) store.close(); } catch (Exception ignored) {}
        }
    }

    public static String getTextFromMessage(Part part) throws Exception {
        StringBuilder result = new StringBuilder();

        if (part.isMimeType("text/plain")) {
            result.append(part.getContent());
        } 
        else if (part.isMimeType("text/html")) {
            String html = (String) part.getContent();
            result.append(Jsoup.parse(html).text());   // Convert HTML to plain text
        } 
        else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();

                // Skip attachments
                if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
                    continue;
                }

                // Important: Prefer text/plain over html if both exist
                if (bodyPart.isMimeType("text/plain")) {
                    result.append(getTextFromMessage(bodyPart));
                    break;                    // ←←← THIS IS THE KEY FIX
                } 
                else if (bodyPart.isMimeType("text/html")) {
                    result.append(getTextFromMessage(bodyPart));
                } 
                else if (bodyPart.isMimeType("multipart/*")) {
                    result.append(getTextFromMessage(bodyPart));
                }
            }
        }

        return result.toString().trim();
    }
}