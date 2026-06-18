package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        // Bootstraps the Spring context and starts the embedded server.
        SpringApplication.run(DemoApplication.class, args);
    }
}
