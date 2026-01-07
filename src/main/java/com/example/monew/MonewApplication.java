package com.example.monew;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MonewApplication {

    public static void main(String[] args) {
        SpringApplication.run(MonewApplication.class, args);
    }

}
