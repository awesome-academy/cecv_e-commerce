package com.example.cecv_e_commerce.service;

public interface MailService {

    void sendActivationEmail(String to, String name, String activationLink);

    void sendPasswordResetEmail(String to, String name, String resetLink);

}
