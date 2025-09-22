package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.challenges.ChallengeResponse;
import com.project.thelittlethings.dto.challenges.UpdateChallengeRequest;
import com.project.thelittlethings.services.ChallengeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService service;

    public ChallengeController(ChallengeService service) {
        this.service = service;
    }

    /**
     * List challenges for a user.
     * status can be: active | completed | declined | archived (optional; defaults to active)
     */
    @GetMapping
    public List<ChallengeResponse> list(
            @RequestParam Long userId,
            @RequestParam(required = false, defaultValue = "active") String status
    ) {
        return service.listByUser(userId, status);
    }

    /**
     * Generate (or refresh) system challenges based on the user's past wins.
     * Returns the freshly generated active challenges.
     */
    @PostMapping("/generate")
    public List<ChallengeResponse> generate(@RequestParam Long userId) {
        return service.generateFromWins(userId);
    }

    /**
     * Get a single challenge assigned to the user (ownership/assignment check inside service).
     */
    @GetMapping("/{id}")
    public ChallengeResponse get(@PathVariable Long id, @RequestParam Long userId) {
        return service.getAssigned(id, userId);
    }

    /**
     * Accept a suggested challenge.
     */
    @PostMapping("/{id}/accept")
    public ResponseEntity<ChallengeResponse> accept(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(service.accept(id, userId));
    }

    /**
     * Decline a suggested challenge (kept for history/analytics, not deleted).
     */
    @PostMapping("/{id}/decline")
    public ResponseEntity<Void> decline(@PathVariable Long id, @RequestParam Long userId) {
        service.decline(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Update progress (e.g., +1 step done). The DTO can support either a delta or an absolute value.
     * Example DTO: { "delta": 1 } or { "current": 3 }
     */
    @PostMapping("/{id}/progress")
    public ResponseEntity<ChallengeResponse> updateProgress(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestBody UpdateChallengeRequest req
    ) {
        return ResponseEntity.ok(service.updateProgress(id, userId, req));
    }

//     /**
//      * Mark complete (awards trophies, closes the challenge).
//      * Service should enforce completion rules (e.g., current >= total).
//      */
    @PostMapping("/{id}/complete")
    public ResponseEntity<ChallengeResponse> complete(@PathVariable Long id, @RequestParam Long userId) {
        return ResponseEntity.ok(service.complete(id, userId));
    }

    /**
     * Archive a challenge (hide from active lists without deletion).
     */
    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archive(@PathVariable Long id, @RequestParam Long userId) {
        service.archive(id, userId);
        return ResponseEntity.noContent().build();
    }
}
