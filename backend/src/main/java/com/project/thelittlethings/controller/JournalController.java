package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.journals.CreateJournalRequest;
import com.project.thelittlethings.dto.journals.UpdateJournalRequest;
import com.project.thelittlethings.dto.journals.JournalResponse;
import com.project.thelittlethings.entities.Win;
import com.project.thelittlethings.services.JournalService;
import com.project.thelittlethings.services.UserService;
import com.project.thelittlethings.security.TokenUtil;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/journals")
@CrossOrigin(origins = "http://localhost:5173")
public class JournalController {

    private final JournalService journalService;
    private final UserService userService;

    public JournalController(JournalService journalService, UserService userService) {
        this.journalService = journalService;
        this.userService = userService;
    }

    // Create a new journal entry
    @PostMapping
    public ResponseEntity<?> createJournal(@RequestHeader("Authorization") String token,
                                         @Valid @RequestBody CreateJournalRequest request) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            
            JournalResponse journal = journalService.createJournal(userId, request);
            return ResponseEntity.ok(journal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Get a specific journal entry
    @GetMapping("/{journalId}")
    public ResponseEntity<?> getJournal(@RequestHeader("Authorization") String token,
                                       @PathVariable Long journalId) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            
            JournalResponse journal = journalService.getJournal(journalId, userId);
            return ResponseEntity.ok(journal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Get all journal entries for the user
    @GetMapping
    public ResponseEntity<?> getAllJournals(@RequestHeader("Authorization") String token,
                                          @RequestParam(value = "sort", defaultValue = "date") String sortBy) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            
            List<JournalResponse> journals = journalService.getAllJournals(userId, sortBy);
            return ResponseEntity.ok(journals);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Update a journal entry
    @PutMapping("/{journalId}")
    public ResponseEntity<?> updateJournal(@RequestHeader("Authorization") String token,
                                         @PathVariable Long journalId,
                                         @Valid @RequestBody UpdateJournalRequest request) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            
            JournalResponse journal = journalService.updateJournal(journalId, userId, request);
            return ResponseEntity.ok(journal);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Delete a journal entry
    @DeleteMapping("/{journalId}")
    public ResponseEntity<?> deleteJournal(@RequestHeader("Authorization") String token,
                                         @PathVariable Long journalId) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            
            boolean deleted = journalService.deleteJournal(journalId, userId);
            if (deleted) {
                return ResponseEntity.ok(Map.of("message", "Journal entry deleted successfully"));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Get user's wins for linking
    @GetMapping("/wins")
    public ResponseEntity<?> getUserWins(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            
            List<Win> wins = journalService.getUserWins(userId);
            return ResponseEntity.ok(wins);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    // Helper method to extract user ID from Authorization header
    private Long getUserIdFromAuthToken(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        if (!TokenUtil.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        String username = TokenUtil.extractUsername(token);
        if (username == null) {
            throw new IllegalArgumentException("Invalid token");
        }
        
        var user = userService.findByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        
        return user.getUserId();
    }
}
