package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.users.CreateUserRequest;
import com.project.thelittlethings.dto.users.LoginRequest;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

// testing user stuff
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5432/TheLittleThings",
    "spring.datasource.username=postgres",
    "spring.datasource.password=Fake2468",
    "spring.jpa.hibernate.ddl-auto=none",
    "spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect"
})
class UserServiceTest {

    @Autowired
    UserService userService;
    
    @Autowired
    UserRepository userRepository;


    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerWorks() {
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setGender("Male");
        request.setRegion("TestRegion");

        User response = userService.register(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getUserId() > 0);
        
        var savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Male", savedUser.getGender());
    }

    @Test
    void loginWorks() {
        // register user
        var registerRequest = new CreateUserRequest();
        registerRequest.setUsername("logintest");
        registerRequest.setEmail("login@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("Test");
        registerRequest.setDob(LocalDate.of(1990, 1, 1));
        registerRequest.setGender("Female");
        registerRequest.setRegion("TestRegion");
        
        userService.register(registerRequest);

        var loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("logintest");
        loginRequest.setPassword("password123");
        
        String token = userService.login(loginRequest);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

   

    @Test
    void duplicateUsernameFails() {
        // register first user
        var request1 = new CreateUserRequest();
        request1.setUsername("duplicate");
        request1.setEmail("first@example.com");
        request1.setPassword("password123");
        request1.setFirstName("First");
        request1.setLastName("User");
        request1.setDob(LocalDate.of(1990, 1, 1));
        request1.setGender("Male");
        request1.setRegion("TestRegion");
        
        userService.register(request1);

        //same username
        var request2 = new CreateUserRequest();
        request2.setUsername("duplicate");
        request2.setEmail("second@example.com");
        request2.setPassword("password123");
        request2.setFirstName("Second");
        request2.setLastName("User");
        request2.setDob(LocalDate.of(1991, 1, 1));
        request2.setGender("Female");
        request2.setRegion("TestRegion");

        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(request2);
        });
    }

    @Test
    void genderTest() {
        var request = new CreateUserRequest();
        request.setUsername("gendertest");
        request.setEmail("gender@example.com");
        request.setPassword("password123");
        request.setFirstName("Gender");
        request.setLastName("Test");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setGender("male"); // lowercase
        request.setRegion("TestRegion");

        var response = userService.register(request);

        assertNotNull(response);
        assertEquals("Male", response.getGender()); // should fix case
    }

    @Test
    void findWorks() {
        var request = new CreateUserRequest();
        request.setUsername("findtest");
        request.setEmail("find@example.com");
        request.setPassword("password123");
        request.setFirstName("Find");
        request.setLastName("Test");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setGender("Male");
        request.setRegion("TestRegion");
        
        userService.register(request);

        var found = userService.findByUsername("findtest");

        assertNotNull(found);
        assertEquals("findtest", found.getUsername());
        assertEquals("find@example.com", found.getEmail());
    }

    ;
    }

 
