package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.enums.MailStatus;
import com.example.cecv_e_commerce.domain.enums.MailType;
import com.example.cecv_e_commerce.domain.model.MailLog;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.repository.MailLogRepository;
import com.example.cecv_e_commerce.repository.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MailLogRepository mailLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailServiceImpl mailService;

    private User user;
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_LINK = "http://test.com/activate";
    private static final String MAIL_FROM = "noreply@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setEmail(TEST_EMAIL);
        user.setName(TEST_NAME);

        ReflectionTestUtils.setField(mailService, "mailFrom", MAIL_FROM);
    }

    @Test
    void sendActivationEmail_ShouldSendEmailAndLogSuccess() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test</html>");
        ArgumentCaptor<MailLog> mailLogCaptor = ArgumentCaptor.forClass(MailLog.class);

        mailService.sendActivationEmail(TEST_EMAIL, TEST_NAME, TEST_LINK);

        verify(mailSender).send(any(MimeMessage.class));
        verify(mailLogRepository).save(mailLogCaptor.capture());

        MailLog capturedLog = mailLogCaptor.getValue();
        assertEquals(TEST_EMAIL, capturedLog.getRecipientEmail());
        assertEquals(MailType.ACTIVATION, capturedLog.getMailType());
        assertEquals(MailStatus.SENT, capturedLog.getStatus());
        assertEquals(user.getId(), capturedLog.getUserId());
    }

    @Test
    void sendPasswordResetEmail_ShouldSendEmailAndLogSuccess() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test</html>");
        ArgumentCaptor<MailLog> mailLogCaptor = ArgumentCaptor.forClass(MailLog.class);

        mailService.sendPasswordResetEmail(TEST_EMAIL, TEST_NAME, TEST_LINK);

        verify(mailSender).send(any(MimeMessage.class));
        verify(mailLogRepository).save(mailLogCaptor.capture());

        MailLog capturedLog = mailLogCaptor.getValue();
        assertEquals(TEST_EMAIL, capturedLog.getRecipientEmail());
        assertEquals(MailType.FORGOT_PASSWORD, capturedLog.getMailType());
        assertEquals(MailStatus.SENT, capturedLog.getStatus());
        assertEquals(user.getId(), capturedLog.getUserId());
    }

    @Test
    void sendActivationEmail_WhenMessagingException_ShouldLogFailure() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test</html>");
        doThrow(new RuntimeException("Test error")).when(mailSender).send(any(MimeMessage.class));
        ArgumentCaptor<MailLog> mailLogCaptor = ArgumentCaptor.forClass(MailLog.class);

        mailService.sendActivationEmail(TEST_EMAIL, TEST_NAME, TEST_LINK);

        verify(mailLogRepository).save(mailLogCaptor.capture());

        MailLog capturedLog = mailLogCaptor.getValue();
        assertEquals(TEST_EMAIL, capturedLog.getRecipientEmail());
        assertEquals(MailType.ACTIVATION, capturedLog.getMailType());
        assertEquals(MailStatus.FAILED, capturedLog.getStatus());
        assertNotNull(capturedLog.getErrorMessage());
    }

    @Test
    void sendActivationEmail_WhenUserNotFound_ShouldStillSendEmail() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test</html>");
        ArgumentCaptor<MailLog> mailLogCaptor = ArgumentCaptor.forClass(MailLog.class);

        mailService.sendActivationEmail(TEST_EMAIL, TEST_NAME, TEST_LINK);

        verify(mailSender).send(any(MimeMessage.class));
        verify(mailLogRepository).save(mailLogCaptor.capture());

        MailLog capturedLog = mailLogCaptor.getValue();
        assertEquals(TEST_EMAIL, capturedLog.getRecipientEmail());
        assertEquals(MailType.ACTIVATION, capturedLog.getMailType());
        assertEquals(MailStatus.SENT, capturedLog.getStatus());
        assertNull(capturedLog.getUserId());
    }

    @Test
    void sendActivationEmail_WhenLoggingFails_ShouldNotThrowException() throws MessagingException {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(user));
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html>Test</html>");
        when(mailLogRepository.save(any(MailLog.class))).thenReturn(new MailLog());

        assertDoesNotThrow(() -> mailService.sendActivationEmail(TEST_EMAIL, TEST_NAME, TEST_LINK));
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void logMailEvent_WithLongErrorMessage_ShouldTruncateErrorMessage() {
        String longError = "a".repeat(2500);
        MailServiceImpl mailServiceSpy = spy(mailService);
        when(mailLogRepository.save(any(MailLog.class))).thenReturn(new MailLog());
        ReflectionTestUtils.invokeMethod(mailServiceSpy, "logMailEvent", 1, "to@example.com",
                MailType.ACTIVATION, false, longError);
        ArgumentCaptor<MailLog> captor = ArgumentCaptor.forClass(MailLog.class);
        verify(mailLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getErrorMessage());
        assertEquals(2000, captor.getValue().getErrorMessage().length());
    }
}
