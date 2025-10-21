package com.project.thelittlethings.controller;

import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.dto.users.*;
import com.project.thelittlethings.services.UserService;
import com.project.thelittlethings.security.HMACtokens;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


// handles user authentication and account managment
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // creates new user account and returns auth token
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest req) {
        try {
            // validate request to prevent crashes
            if (req == null) {
                return ResponseEntity.badRequest().body("Request cannot be empty");
            }
            if (req.getUsername() == null || req.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username is required");
            }
            if (req.getEmail() == null || req.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email is required");
            }
            if (req.getPassword() == null || req.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body("Password must be at least 6 characters");
            }
            
            User u = userService.register(req);
            String token = HMACtokens.issueToken(u.getUsername(), 60 * 60 * 24);
            return ResponseEntity.ok(new AuthResponse(token, u.getUserId(), u.getUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // authenticates user and returns token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            // basic checks
            if (req == null) {
                return ResponseEntity.badRequest().body("Request cannot be empty");
            }
            if (req.getUsernameOrEmail() == null || req.getUsernameOrEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Username or email is required");
            }
            if (req.getPassword() == null || req.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }
            
            String token = userService.login(req);
            String username = HMACtokens.extractUsername(token);
            User u = userService.findByUsername(username);
            return ResponseEntity.ok(new AuthResponse(token, u.getUserId(), u.getUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(401).body(ex.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String auth) {
        String token = auth.replaceFirst("Bearer ", "");
        userService.logout(token);
        return ResponseEntity.ok().build();
    }

    // gets current user info from token
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replaceFirst("Bearer ", "");
            if (!HMACtokens.validateToken(token) || userService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(401).build();
            }
            
            String username = HMACtokens.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) return ResponseEntity.status(404).build();
            
            UserResponse response = UserResponse.fromUser(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    // allows user to change their password
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String auth, @RequestBody ChangePasswordRequest req) {
        try {
            if (req == null || req.getOldPassword() == null || req.getNewPassword() == null) {
                return ResponseEntity.badRequest().body("Old and new passwords are required");
            }
            if (req.getNewPassword().length() < 6) {
                return ResponseEntity.badRequest().body("New password must be at least 6 characters");
            }
            
            String token = auth.replaceFirst("Bearer ", "");
            if (!HMACtokens.validateToken(token) || userService.isTokenBlacklisted(token)) return ResponseEntity.status(401).build();
            String username = HMACtokens.extractUsername(token);
            User u = userService.findByUsername(username);
            userService.changePassword(u.getUserId(), req.getOldPassword(), req.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
// allows user to change their username
    @PostMapping("/change-username")
    public ResponseEntity<?> changeUsername(@RequestHeader("Authorization") String auth, @RequestBody ChangeUsernameRequest req) {
        try {
            String token = auth.replaceFirst("Bearer ", "");
            if (!HMACtokens.validateToken(token) || userService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(401).build();
            }
            
            String username = HMACtokens.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) return ResponseEntity.status(404).build();
            
            String newToken = userService.changeUsername(user.getUserId(), req.getNewUsername());
            return ResponseEntity.ok(new AuthResponse(newToken, user.getUserId(), req.getNewUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }// allows user to delete their account

    @DeleteMapping("/")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replaceFirst("Bearer ", "");
            if (!HMACtokens.validateToken(token) || userService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(401).build();
            }
            
            String username = HMACtokens.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) return ResponseEntity.status(404).build();
            
            boolean deleted = userService.deleteUser(user.getUserId());
            userService.logout(token);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(500).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    // allows user to reset their password

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            userService.resetPassword(req.getEmail(), "temp123456");
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
