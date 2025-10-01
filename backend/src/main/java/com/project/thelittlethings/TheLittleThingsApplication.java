package com.project.thelittlethings;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TheLittleThingsApplication {


	public static void main(String[] args) {
		SpringApplication.run(TheLittleThingsApplication.class, args);
	}
	@Bean
    CommandLineRunner demoToken() {
        return args -> {
            String token = com.project.thelittlethings.security.HMACtokens.issueToken("test3", 3600);
            System.out.println("Generated Token: " + token);
        };
    }

	
}

