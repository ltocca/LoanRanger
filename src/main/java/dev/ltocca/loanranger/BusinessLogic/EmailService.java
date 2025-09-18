package dev.ltocca.loanranger.BusinessLogic;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailService {
    private final String host;
    private final String port;
    private final String username;
    private final String password;
    private final boolean enabled;

    public EmailService() {
        // Load configuration from properties file or environment variables
        this.host = System.getProperty("mail.host", "smtp.gmail.com");
        this.port = System.getProperty("mail.port", "587");
        this.username = System.getProperty("mail.username", "");
        this.password = System.getProperty("mail.password", "");
        this.enabled = Boolean.parseBoolean(System.getProperty("mail.enabled", "false"));
    }

    public void sendEmail(String to, String subject, String body) {
        if (!enabled) {
            System.out.println("Email notification (simulated):");
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body: " + body);
            return;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);
            System.out.println("Email sent successfully to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }
}