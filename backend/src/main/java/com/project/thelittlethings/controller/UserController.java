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
            if (!TokenUtil.validateToken(token) || userService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(401).build();
            }
            
            String username = TokenUtil.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) return ResponseEntity.status(404).build();
            
            UserResponse response = UserResponse.fromUser(user);
            return ResponseEntity.ok(response);
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

    @PostMapping("/change-username")
    public ResponseEntity<?> changeUsername(@RequestHeader("Authorization") String auth, @RequestBody ChangeUsernameRequest req) {
        try {
            String token = auth.replaceFirst("Bearer ", "");
            if (!TokenUtil.validateToken(token) || userService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(401).build();
            }
            
            String username = TokenUtil.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) return ResponseEntity.status(404).build();
            
            String newToken = userService.changeUsername(user.getUserId(), req.getNewUsername());
            return ResponseEntity.ok(new AuthResponse(newToken, user.getUserId(), req.getNewUsername()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping("/")
    public ResponseEntity<?> deleteAccount(@RequestHeader("Authorization") String auth) {
        try {
            String token = auth.replaceFirst("Bearer ", "");
            if (!TokenUtil.validateToken(token) || userService.isTokenBlacklisted(token)) {
                return ResponseEntity.status(401).build();
            }
            
            String username = TokenUtil.extractUsername(token);
            User user = userService.findByUsername(username);
            if (user == null) return ResponseEntity.status(404).build();
            
            boolean deleted = userService.deleteUser(user.getUserId());
            userService.logout(token);
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(500).build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

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
