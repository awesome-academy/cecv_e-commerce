package com.example.cecv_e_commerce.service.impl;

import com.example.cecv_e_commerce.domain.model.MailLog;
import com.example.cecv_e_commerce.domain.model.MailStatus;
import com.example.cecv_e_commerce.domain.model.MailType;
import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.repository.MailLogRepository;
import com.example.cecv_e_commerce.repository.UserRepository;
import com.example.cecv_e_commerce.service.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Optional;

@Service
public class MailServiceImpl implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private MailLogRepository mailLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public void sendActivationEmail(String to, String name, String activationLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("activationLink", activationLink);
        String htmlContent = templateEngine.process("activation-email", context);
        String subject = "Activate Your Account";
        sendHtmlEmailAndLog(to, subject, htmlContent, MailType.ACTIVATION);
    }

    @Override
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("resetLink", resetLink);
        String htmlContent = templateEngine.process("password-reset-email", context);
        String subject = "Reset Your Password";
        sendHtmlEmailAndLog(to, subject, htmlContent, MailType.FORGOT_PASSWORD);
    }

    @Async
    protected void sendHtmlEmailAndLog(String to, String subject, String htmlContent, MailType mailType) {
        logger.info("Attempting to send {} email to {}", mailType, to);
        Integer userId = findUserIdByEmail(to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            helper.setFrom(mailFrom);

            mailSender.send(message);
            logger.info("{} email sent successfully to {}", mailType, to);
            logMailEvent(userId, to, mailType, true, null);
        } catch (MessagingException e) {
            logger.error("Failed to send {} email to {}: {}", mailType, to, e.getMessage());
            logMailEvent(userId, to, mailType, false, e.getMessage());
        } catch (Exception e) {
            logger.error("An unexpected error occurred while sending {} email to {}: {}", mailType, to, e.getMessage());
            logMailEvent(userId, to, mailType, false, e.getMessage());
        }
    }

    private void logMailEvent(Integer userId, String recipientEmail, MailType mailType, boolean success, String errorMessage) {
        try {
            MailLog logEntry = new MailLog();
            logEntry.setUserId(userId);
            logEntry.setRecipientEmail(recipientEmail);
            logEntry.setMailType(mailType);
            logEntry.setStatus(success ? MailStatus.SENT : MailStatus.FAILED);
            if (!success) {
                logEntry.setErrorMessage(errorMessage != null && errorMessage.length() > 2000 ? errorMessage.substring(0, 2000) : errorMessage); // Cắt ngắn lỗi nếu quá dài
            }
            mailLogRepository.save(logEntry);
        } catch (Exception e) {
            logger.error("Failed to log mail event to DB for recipient {}: {}", recipientEmail, e.getMessage());
        }
    }

    private Integer findUserIdByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.map(User::getId).orElse(null);
    }
}
