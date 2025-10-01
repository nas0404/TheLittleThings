package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.journals.CreateJournalRequest;
import com.project.thelittlethings.dto.journals.UpdateJournalRequest;
import com.project.thelittlethings.dto.journals.JournalResponse;
import com.project.thelittlethings.entities.Win;
import com.project.thelittlethings.services.JournalService;
import com.project.thelittlethings.services.UserService;
import com.project.thelittlethings.security.HMACtokens;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<?> createJournal(@RequestHeader("Authorization") String token,
                                         @RequestBody CreateJournalRequest request) {
        try {
            // basic validation - prevent crashes
            if (request == null) {
                return ResponseEntity.badRequest().body("Request cannot be empty");
            }
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Title is required");
            }
            if (request.getContent() == null) {
                return ResponseEntity.badRequest().body("Content is required");
            }
            
            Long userId = getUserIdFromAuthToken(token);
            JournalResponse journal = journalService.createJournal(userId, request);
            return ResponseEntity.ok(journal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{journalId}")
    public ResponseEntity<?> getJournal(@RequestHeader("Authorization") String token,
                                       @PathVariable Long journalId) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            JournalResponse journal = journalService.getJournal(journalId, userId);
            return ResponseEntity.ok(journal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllJournals(@RequestHeader("Authorization") String token,
                                          @RequestParam(value = "sort", defaultValue = "date") String sortBy) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            List<JournalResponse> journals = journalService.getAllJournals(userId, sortBy);
            return ResponseEntity.ok(journals);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{journalId}")
    public ResponseEntity<?> updateJournal(@RequestHeader("Authorization") String token,
                                         @PathVariable Long journalId,
                                         @RequestBody UpdateJournalRequest request) {
        try {
            // check inputs to avoid crashes
            if (journalId == null || journalId <= 0) {
                return ResponseEntity.badRequest().body("Invalid journal ID");
            }
            if (request == null) {
                return ResponseEntity.badRequest().body("Request cannot be empty");
            }
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Title is required");
            }
            
            Long userId = getUserIdFromAuthToken(token);
            JournalResponse journal = journalService.updateJournal(journalId, userId, request);
            return ResponseEntity.ok(journal);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{journalId}")
    public ResponseEntity<?> deleteJournal(@RequestHeader("Authorization") String token,
                                         @PathVariable Long journalId) {
        try {
            if (journalId == null || journalId <= 0) {
                return ResponseEntity.badRequest().body("Invalid journal ID");
            }
            
            Long userId = getUserIdFromAuthToken(token);
            journalService.deleteJournal(journalId, userId);
            return ResponseEntity.ok("Deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/wins")
    public ResponseEntity<?> getUserWins(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromAuthToken(token);
            List<Win> wins = journalService.getUserWins(userId);
            return ResponseEntity.ok(wins);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Long getUserIdFromAuthToken(String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        
        if (!HMACtokens.validateToken(token)) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        
        String username = HMACtokens.extractUsername(token);
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
