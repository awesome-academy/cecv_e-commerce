package com.example.cecv_e_commerce.scheduled;

import com.example.cecv_e_commerce.domain.model.User;
import com.example.cecv_e_commerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AccountCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(AccountCleanupTask.class);

    @Autowired
    private UserRepository userRepository;

    @Scheduled(cron = "${app.scheduling.account-cleanup.cron:0 0 2 * * ?}")
    @Transactional
    public void cleanupInactiveAccounts() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Running inactive account cleanup task at {}", now);

        List<User> inactiveUsers = userRepository.findByIsActiveFalseAndActivationDeadlineBefore(now);

        if (inactiveUsers.isEmpty()) {
            logger.info("No inactive accounts found with expired activation tokens.");
            return;
        }

        logger.info("Found {} inactive accounts with expired activation tokens to cleanup.", inactiveUsers.size());

        try {
            userRepository.deleteAll(inactiveUsers);
            logger.info("Successfully cleaned up {} inactive accounts.", inactiveUsers.size());
        } catch (Exception e) {
            logger.error("Error during inactive account cleanup: {}", e.getMessage(), e);
        }
    }
}
