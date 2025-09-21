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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testRegisterUser_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setGender("Male");
        request.setRegion("TestRegion");

        // Act
        User response = userService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertTrue(response.getUserId() > 0);
        
        // Verify user was saved
        User savedUser = userRepository.findByUsername("testuser").orElse(null);
        assertNotNull(savedUser);
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("Male", savedUser.getGender()); // Should be normalized
    }

    @Test
    void testLogin_Success() {
        // Arrange - First register a user
        CreateUserRequest registerRequest = new CreateUserRequest();
        registerRequest.setUsername("logintest");
        registerRequest.setEmail("login@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFirstName("Login");
        registerRequest.setLastName("Test");
        registerRequest.setDob(LocalDate.of(1990, 1, 1));
        registerRequest.setGender("Female");
        registerRequest.setRegion("TestRegion");
        
        userService.register(registerRequest);

        // Act - Now login
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("logintest");
        loginRequest.setPassword("password123");
        
        String token = userService.login(loginRequest);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("nonexistent");
        loginRequest.setPassword("wrongpassword");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userService.login(loginRequest);
        });
    }

    @Test
    void testRegisterUser_DuplicateUsername() {
        // Arrange - Register first user
        CreateUserRequest request1 = new CreateUserRequest();
        request1.setUsername("duplicate");
        request1.setEmail("first@example.com");
        request1.setPassword("password123");
        request1.setFirstName("First");
        request1.setLastName("User");
        request1.setDob(LocalDate.of(1990, 1, 1));
        request1.setGender("Male");
        request1.setRegion("TestRegion");
        
        userService.register(request1);

        // Act & Assert - Try to register with same username
        CreateUserRequest request2 = new CreateUserRequest();
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
    void testGenderNormalization() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("gendertest");
        request.setEmail("gender@example.com");
        request.setPassword("password123");
        request.setFirstName("Gender");
        request.setLastName("Test");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setGender("male"); // lowercase should be normalized to "Male"
        request.setRegion("TestRegion");

        // Act
        User response = userService.register(request);

        // Assert
        assertNotNull(response);
        assertEquals("Male", response.getGender()); // Should be normalized to proper case
    }

    @Test
    void testFindByUsername_Success() {
        // Arrange
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("findtest");
        request.setEmail("find@example.com");
        request.setPassword("password123");
        request.setFirstName("Find");
        request.setLastName("Test");
        request.setDob(LocalDate.of(1990, 1, 1));
        request.setGender("Male");
        request.setRegion("TestRegion");
        
        userService.register(request);

        // Act
        User found = userService.findByUsername("findtest");

        // Assert
        assertNotNull(found);
        assertEquals("findtest", found.getUsername());
        assertEquals("find@example.com", found.getEmail());
    }

    @Test
    void testFindByUsername_NotFound() {
        // Act
        User found = userService.findByUsername("nonexistent");

        // Assert
        assertNull(found);
    }
}