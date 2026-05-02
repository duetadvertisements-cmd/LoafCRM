import jakarta.mail.*;
import jakarta.mail.search.FlagTerm;
import javaServers.handlers.MakeTicketUsingEmailReader;
import org.jsoup.Jsoup;

import java.util.Properties;

public class MailReader {
    public static void main(String[] args) {
        try {
            Properties props = new Properties();
            props.put("mail.store.protocol", "imaps");

            Session session = Session.getInstance(props);
            Store store = session.getStore("imaps");

            store.connect("imap.gmail.com", "loafcrm@gmail.com", "ljevikvnedqqsnql");

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] unread =
                inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));

            for (Message msg : unread) {

                System.out.println("Subject: " + msg.getSubject());
                System.out.println("From: " + msg.getFrom()[0]);
                System.out.println("----------------");

                Address[] from = msg.getFrom();
                String senderAddress = from[0].toString();

                String Message = getTextFromMessage (msg);
                Message = Jsoup.parse(Message).text();


                MakeTicketUsingEmailReader ticketCreator = new MakeTicketUsingEmailReader ();
                ticketCreator.makeTicket (msg.getSubject (), Message, senderAddress);

                // Mark as read
                // msg.setFlag(Flags.Flag.SEEN, true);
            }

            inbox.close(true);
            store.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getTextFromMessage(Part part) throws Exception {

        if (part.isMimeType("text/plain")) {
            return (String) part.getContent();
        }

        if (part.isMimeType("text/html")) {
            return (String) part.getContent(); // raw HTML
        }

        if (part.isMimeType("multipart/*")) {

            Multipart multipart = (Multipart) part.getContent();
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);

                String disposition = bodyPart.getDisposition();

                // Skip attachments
                if (Part.ATTACHMENT.equalsIgnoreCase(disposition)) {
                    continue;
                }

                result.append(getTextFromMessage(bodyPart));
            }

            return result.toString();
        }

        return "";
    }
}