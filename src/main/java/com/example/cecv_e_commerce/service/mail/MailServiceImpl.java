package com.example.cecv_e_commerce.service.mail;

import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailService {

    @Override
    public void sendActivationEmail(String to, String name, String activationLink) {
    }

    @Override
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
    }
}
