package com.project.thelittlethings.services;
import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.UserRepository;

public  class UserService {

     private final UserRepository userRepo;

     public UserService(UserRepository userRepo) {
         this.userRepo = userRepo;
     }

    
}