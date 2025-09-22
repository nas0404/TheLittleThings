package com.project.thelittlethings;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.dto.test;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.services.UserService;


@SpringBootApplication
public class TheLittleThingsApplication {


	public static void main(String[] args) {
		SpringApplication.run(TheLittleThingsApplication.class, args);
	}

	
}

