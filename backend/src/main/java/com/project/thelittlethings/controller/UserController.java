package com.project.thelittlethings.controller;

import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.dto.users.*;
import com.project.thelittlethings.services.UserService;
import com.project.thelittlethings.security.TokenUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody CreateUserRequest req) {
        try {
            User u = userService.register(req);
            String token = TokenUtil.issueToken(u.getUsername(), 60 * 60 * 24);
            return ResponseEntity.ok(new AuthResponse(token, u.getUserId(), u.getUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = userService.login(req);
            String username = TokenUtil.extractUsername(token);
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

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replaceFirst("Bearer ", "");
            if (!TokenUtil.validateToken(token) || userService.isTokenBlacklisted(token)) return ResponseEntity.status(401).build();
            String username = TokenUtil.extractUsername(token);
            User u = userService.findByUsername(username);
            if (u == null) return ResponseEntity.status(404).build();
            // Map to DTO so we can include a human-readable lastLogin without changing User.java
            com.project.thelittlethings.dto.users.UserResponse resp = com.project.thelittlethings.dto.users.UserResponse.fromUser(u);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String auth, @RequestBody ChangePasswordRequest req) {
        try {
            String token = auth.replaceFirst("Bearer ", "");
            if (!TokenUtil.validateToken(token) || userService.isTokenBlacklisted(token)) return ResponseEntity.status(401).build();
            String username = TokenUtil.extractUsername(token);
            User u = userService.findByUsername(username);
            userService.changePassword(u.getUserId(), req.getOldPassword(), req.getNewPassword());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest req) {
        try {
            // In real app we'd email a reset link. For demo accept new password param or set default
            userService.resetPassword(req.getEmail(), "new-temporary-password");
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
