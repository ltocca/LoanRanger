/*
package dev.ltocca.loanranger.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Initialize with dummy SMTP properties
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.test.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(props, null); // No authenticator for testing
        emailService = new EmailService(session, "noreply@test.com");
    }

    @Test
    void sendEmail_shouldCreateMessageAndSendIt() throws MessagingException {
        // Given
        String to = "recipient@example.com";
        String subject = "Test Subject";
        String body = "Test Body";
        Session mockSession = mock(Session.class);
        EmailService serviceUnderTest = new EmailService(mockSession, "noreply@test.com");

        MimeMessage expectedMessage = new MimeMessage(mockSession);
        expectedMessage.setFrom("noreply@test.com");
        expectedMessage.setRecipients(Message.RecipientType.TO, to);
        expectedMessage.setSubject(subject);
        expectedMessage.setText(body);

        try (MockedConstruction<Transport> mockedTransportConstruction = mockConstruction(Transport.class)) {
            Transport mockTransport = mockedTransportConstruction.constructed().get(0);

            // When
            serviceUnderTest.sendEmail(to, subject, body);

            // Then
            verify(mockTransport, times(1)).connect(any(), any(), any()); // Verify connect was called
            ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
            verify(mockTransport, times(1)).sendMessage(messageCaptor.capture(), any()); // Capture the sent message

            MimeMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getFrom()[0].toString()).isEqualTo("noreply@test.com");
            assertThat(capturedMessage.getRecipients(Message.RecipientType.TO)[0].toString()).isEqualTo(to);
            assertThat(capturedMessage.getSubject()).isEqualTo(subject);
            assertThat(capturedMessage.getContent().toString()).isEqualTo(body);
        }
    }

    // Note: Testing sendEmail failure is tricky without changing the EmailService implementation
    // to make Transport handling more injectable or by using a wrapper for Transport.
    // The current implementation catches MessagingException internally and prints to stderr.
    // For now, we focus on the successful path.
}*/
