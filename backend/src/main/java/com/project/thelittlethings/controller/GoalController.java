package com.project.thelittlethings.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import com.project.thelittlethings.services.GoalService;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping("/{id}/complete")
    public ResponseEntity<String> completeGoal(@PathVariable Long id) {
        goalService.completeGoal(id);
        return ResponseEntity.ok("Goal completed and Win recorded.");
    }
}