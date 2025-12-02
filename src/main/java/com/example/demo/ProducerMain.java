package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ProducerMain {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ProducerMain.class);
        app.setAdditionalProfiles("producer");
        app.run(args);
    }
}