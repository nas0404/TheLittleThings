package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.wins.CreateWinRequest;
import com.project.thelittlethings.dto.wins.UpdateWinRequest;
import com.project.thelittlethings.dto.wins.WinResponse;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.security.HMACtokens;
import com.project.thelittlethings.services.UserService;
import com.project.thelittlethings.services.WinService;

import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/wins")
@CrossOrigin(origins = {"http://localhost:5173/", "https://thelittlethings.azurewebsites.net/"}, allowCredentials = "true")
public class WinController {

    private final WinService service;
    private final UserService userService;

    public WinController(WinService service, UserService userService) {
        this.service = service;
        this.userService = userService;
    }

    // Shared private method to extract user ID from JWT
    private Long userIdFromAuth(String authHeader) {
        final String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : authHeader;

        if (!HMACtokens.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        final String username = HMACtokens.extractUsername(token);
        if (username == null)
            throw new IllegalArgumentException("Invalid token");

        User u = userService.findByUsername(username);
        if (u == null)
            throw new IllegalArgumentException("User not found");

        return u.getUserId();
    }

    // Create Win
    @PostMapping
    public ResponseEntity<WinResponse> create(@RequestHeader("Authorization") String auth,
            @RequestBody CreateWinRequest req,
            UriComponentsBuilder uri) {
        Long userId = userIdFromAuth(auth);

        req.setUserId(userId);

        WinResponse created = service.createWin(req);

        URI location = uri.path("/api/wins/{id}")
                .buildAndExpand(created.getWinId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    // List all wins
    @GetMapping
    public ResponseEntity<List<WinResponse>> list(@RequestHeader("Authorization") String auth) {
        Long userId = userIdFromAuth(auth);
        return ResponseEntity.ok(service.listWinsByUser(userId));
    }

    // Get one win
    @GetMapping("/{id}")
    public ResponseEntity<WinResponse> get(@RequestHeader("Authorization") String auth,
            @PathVariable Long id) {
        Long userId = userIdFromAuth(auth);
        return ResponseEntity.ok(service.getWinById(id, userId));
    }

    // Update win
    @PutMapping("/{id}")
    public ResponseEntity<WinResponse> update(@RequestHeader("Authorization") String auth,
            @PathVariable Long id,
            @RequestBody UpdateWinRequest req) {
        Long userId = userIdFromAuth(auth);
        return ResponseEntity.ok(service.updateWin(id, userId, req));
    }

    // Delete win
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestHeader("Authorization") String auth,
            @PathVariable Long id) {
        Long userId = userIdFromAuth(auth);
        service.deleteWin(id, userId);
        return ResponseEntity.noContent().build();
    }
}