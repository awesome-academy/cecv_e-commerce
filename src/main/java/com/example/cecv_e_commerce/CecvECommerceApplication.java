package com.example.cecv_e_commerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CecvECommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CecvECommerceApplication.class, args);
    }
}
