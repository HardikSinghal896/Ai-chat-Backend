package com.spur.chat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.spur.chat")
public class AiLiveChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiLiveChatApplication.class, args);
    }
}
