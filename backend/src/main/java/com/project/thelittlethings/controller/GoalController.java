package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.goals.CreateGoalRequest;
import com.project.thelittlethings.dto.goals.GoalResponse;
import com.project.thelittlethings.dto.goals.UpdateGoalRequest;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.security.HMACtokens;
import com.project.thelittlethings.services.GoalService;
import com.project.thelittlethings.services.UserService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.net.URI;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/goals")
@Validated
public class GoalController {

  private final GoalService goalService;
  private final UserService userService;

  public GoalController(GoalService goalService, UserService userService) {
    this.goalService = goalService;
    this.userService = userService;
  }

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

  @PostMapping
  public ResponseEntity<?> create(@RequestHeader("Authorization") String auth,
      @Valid @RequestBody CreateGoalRequest req,
      UriComponentsBuilder uri) {
    try {
      Long userId = userIdFromAuth(auth);
      GoalResponse created = goalService.create(userId, req);
      URI location = uri.path("/api/goals/{goalId}")
          .buildAndExpand(created.getGoalId())
          .toUri();
      return ResponseEntity.created(location).body(created);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @GetMapping
  public ResponseEntity<?> list(@RequestHeader("Authorization") String auth,
      @RequestParam(required = false) @Positive(message = "category must be valid") Long categoryId) {
    try {
      Long userId = userIdFromAuth(auth);
      List<GoalResponse> out = (categoryId == null)
          ? goalService.listGoalsByUser(userId)
          : goalService.listGoalsByUserAndCategory(userId, categoryId);
      return ResponseEntity.ok(out);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @GetMapping("/grouped")
  public ResponseEntity<?> listGrouped(@RequestHeader("Authorization") String auth,
      @RequestParam(required = false) @Positive(message = "category must be valid") Long categoryId,
      @RequestParam(required = false) @Pattern(regexp = "^(?i)(HIGH|MEDIUM|LOW)$", message = "priority must be HIGH, MEDIUM, or LOW") String priority) {
    try {
      Long userId = userIdFromAuth(auth);
      Map<String, List<GoalResponse>> grouped = goalService.listGrouped(userId, categoryId, priority);
      return ResponseEntity.ok(grouped);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @GetMapping("/{goalId}")
  public ResponseEntity<?> getOne(@RequestHeader("Authorization") String auth,
      @PathVariable @Positive(message = "goalId must be positive") Long goalId) {
    try {
      Long userId = userIdFromAuth(auth);
      return ResponseEntity.ok(goalService.getOwnedGoal(goalId, userId));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @PutMapping("/{goalId}")
  public ResponseEntity<?> update(@RequestHeader("Authorization") String auth,
      @PathVariable @Positive(message = "goalId must be positive") Long goalId,
      @Valid @RequestBody UpdateGoalRequest req) {
    try {
      Long userId = userIdFromAuth(auth);
      return ResponseEntity.ok(goalService.updateGoal(goalId, userId, req));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @DeleteMapping("/{goalId}")
  public ResponseEntity<?> delete(@RequestHeader("Authorization") String auth,
      @PathVariable @Positive(message = "goalId must be positive") Long goalId) {
    try {
      Long userId = userIdFromAuth(auth);
      goalService.delete(goalId, userId);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @PostMapping("/{goalId}/complete")
  public ResponseEntity<?> complete(@RequestHeader("Authorization") String auth,
      @PathVariable @Positive(message = "goalId must be positive") Long goalId) {
    try {
      long userId = userIdFromAuth(auth);
      goalService.getOwnedGoal(goalId, userId);
      goalService.completeGoal(goalId);
      return ResponseEntity.ok("Goal completed and Win recorded.");
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }
}
