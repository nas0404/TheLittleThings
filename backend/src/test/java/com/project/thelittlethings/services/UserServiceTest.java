package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.users.CreateUserRequest;
import com.project.thelittlethings.dto.users.LoginRequest;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDate;
import java.util.Optional;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock UserRepository userRepo;
    
    UserService userService;
    
    User testUser;

    private String hash(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(s.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepo);
        
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword(hash("password123"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setGender("Male");
        testUser.setRegion("Australia");
    }

    @Test
    void testRegister() {
        when(userRepo.existsByUsername("testuser")).thenReturn(false);
        when(userRepo.save(any(User.class))).thenReturn(testUser);

        var request = new CreateUserRequest();
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
        verify(userRepo).save(any(User.class));
    }

    @Test
    void testRegisterWithDuplicateUsername() {
        when(userRepo.existsByUsername("duplicate")).thenReturn(true);

        var request = new CreateUserRequest();
        request.setUsername("duplicate");
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("Test");
        request.setLastName("User");

        assertThrows(IllegalArgumentException.class, 
            () -> userService.register(request));
    }

    @Test
    void testLogin() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        var loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");
        
        String token = userService.login(loginRequest);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void testLoginWithInvalidUser() {
        when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        var loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("nonexistent");
        loginRequest.setPassword("password123");

        assertThrows(IllegalArgumentException.class, 
            () -> userService.login(loginRequest));
    }

    @Test
    void testFindByUsername() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        User found = userService.findByUsername("testuser");

        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }
}

 
