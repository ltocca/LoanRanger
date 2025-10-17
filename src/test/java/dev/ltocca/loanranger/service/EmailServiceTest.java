package dev.ltocca.loanranger.service;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;
    private GreenMail greenMail;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, false, "test@library.com");
    }

    // Unit Tests
    @Test
    void sendEmail_whenEnabled_callsMailSender() {
        EmailService enabledEmailService = new EmailService(mailSender, true, "test@library.com");

        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));

        enabledEmailService.sendEmail("recipient@example.com", "Test Subject", "Test Body");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_whenDisabled_doesNotCallMailSender() {
        emailService.sendEmail("recipient@example.com", "Test Subject", "Test Body");

        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_whenMailSenderThrowsException_logsError() {
        EmailService enabledEmailService = new EmailService(mailSender, true, "test@library.com");
        when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));


        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        enabledEmailService.sendEmail("recipient@example.com", "Test Subject", "Test Body");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_whenMessagingExceptionOccurs_logsError() {
        EmailService enabledEmailService = new EmailService(mailSender, true, "loanranger@mail.ltocca.dev");
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Messaging exception"));

        enabledEmailService.sendEmail("me@mail.ltocca.dev", "Test Subject", "Test Body");

        verify(mailSender).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }


    @Test
    void sendEmail_shouldSendAndReceiveMessageSuccessfully() throws MessagingException, IOException {
        greenMail = new GreenMail(ServerSetupTest.SMTP);
        greenMail.setUser("testuser", "testpass");
        greenMail.start();

        try {
            JavaMailSender realMailSender = createRealMailSender();
            EmailService realEmailService = new EmailService(realMailSender, true, "test@library.org");
            String recipient = "recipient@example.com";
            String subject = "Test Subject";
            String body = "This is the test body.";

            realEmailService.sendEmail(recipient, subject, body);

            assertThat(greenMail.waitForIncomingEmail(5000, 1)).isTrue();

            MimeMessage[] receivedMessages = greenMail.getReceivedMessages();
            assertThat(receivedMessages).hasSize(1);
            MimeMessage receivedMessage = receivedMessages[0];

            assertThat(receivedMessage.getAllRecipients()[0].toString()).isEqualTo(recipient);
            assertThat(receivedMessage.getSubject()).isEqualTo(subject);

            Object outerContent = receivedMessage.getContent();
            assertThat(outerContent).isInstanceOf(MimeMultipart.class);
            MimeMultipart outerMultipart = (MimeMultipart) outerContent;

            assertThat(outerMultipart.getCount()).isGreaterThanOrEqualTo(1);
            Object innerContent = outerMultipart.getBodyPart(0).getContent();
            assertThat(innerContent).isInstanceOf(MimeMultipart.class);
            MimeMultipart innerMultipart = (MimeMultipart) innerContent;

            assertThat(innerMultipart.getCount()).isGreaterThanOrEqualTo(1);
            Object textContentObject = innerMultipart.getBodyPart(0).getContent();
            assertThat(textContentObject).isInstanceOf(String.class);
            String textContent = (String) textContentObject;

            assertThat(textContent.trim()).isEqualTo(body.trim());

        } finally {
            if (greenMail != null) {
                greenMail.stop();
            }
        }
    }

    @AfterEach
    void tearDown() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    private JavaMailSender createRealMailSender() {
        org.springframework.mail.javamail.JavaMailSenderImpl sender = new org.springframework.mail.javamail.JavaMailSenderImpl();
        sender.setHost("localhost");
        sender.setPort(greenMail.getSmtp().getPort());
        sender.setUsername("testuser");
        sender.setPassword("testpass");
        sender.setProtocol("smtp");
        java.util.Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.debug", "false");
        return sender;
    }
}