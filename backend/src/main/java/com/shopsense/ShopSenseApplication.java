package com.shopsense;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShopSenseApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShopSenseApplication.class, args);
    }
}
