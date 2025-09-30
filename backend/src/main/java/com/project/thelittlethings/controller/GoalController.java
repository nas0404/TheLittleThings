package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.goals.CreateGoalRequest;
import com.project.thelittlethings.dto.goals.GoalResponse;
import com.project.thelittlethings.dto.goals.UpdateGoalRequest;
import com.project.thelittlethings.services.GoalService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/goals")
@Validated // enables validation on method params (@RequestParam/@PathVariable)
public class GoalController {

  private final GoalService service;

  public GoalController(GoalService service) {
    this.service = service;
  }

  //Create a goal and categoryId comes from the request body.
  @PostMapping
  public ResponseEntity<GoalResponse> create(@PathVariable @Positive(message = "userId must be positive") Long userId,
                                             @Valid @RequestBody CreateGoalRequest req,
                                             UriComponentsBuilder uri) {
    var created = service.createGoal(userId, req.getCategoryId(), req);
    URI location = uri.path("/api/users/{userId}/goals/{goalId}")
        .buildAndExpand(userId, created.getGoalId())
        .toUri();
    return ResponseEntity.created(location).body(created);
  }

  // List all goals for a user, or filter by categoryId. 
  @GetMapping
  public List<GoalResponse> list(@PathVariable @Positive(message = "userId must be positive") Long userId,
                                 @RequestParam(required = false)
                                 @Positive(message = "categoryId must be a positive number")
                                 Long categoryId) {
    return (categoryId == null)
        ? service.listGoalsByUser(userId)
        : service.listGoalsByUserAndCategory(userId, categoryId);
  }

  // Grouped listing by priority (HIGH/MEDIUM/LOW). Optional categoryId & priority filter.
  @GetMapping("/grouped")
  public Map<String, List<GoalResponse>> listGrouped(@PathVariable @Positive(message = "userId must be positive") Long userId,
                                                     @RequestParam(required = false)
                                                     @Positive(message = "categoryId must be a positive number")
                                                     Long categoryId,
                                                     @RequestParam(required = false)
                                                     @Pattern(regexp = "^(?i)(HIGH|MEDIUM|LOW)$",
                                                              message = "priority must be HIGH, MEDIUM, or LOW")
                                                     String priority) {
    return service.listGrouped(userId, categoryId, priority);
  }

  // Get a single goal
  @GetMapping("/{goalId}")
  public GoalResponse getOne(@PathVariable @Positive(message = "userId must be positive") Long userId,
                             @PathVariable @Positive(message = "goalId must be positive") Long goalId) {
    return service.getOwnedGoal(goalId, userId);
  }

  // Update a goal (fully or partially); can also move category.
  @PutMapping("/{goalId}")
  public ResponseEntity<GoalResponse> update(@PathVariable @Positive(message = "userId must be positive") Long userId,
                                             @PathVariable @Positive(message = "goalId must be positive") Long goalId,
                                             @Valid @RequestBody UpdateGoalRequest req) {
    return ResponseEntity.ok(service.updateGoal(goalId, userId, req));
  }

  //Delete a goal. 
  @DeleteMapping("/{goalId}")
  public ResponseEntity<Void> delete(@PathVariable @Positive(message = "userId must be positive") Long userId,
                                     @PathVariable @Positive(message = "goalId must be positive") Long goalId) {
    service.delete(goalId, userId);
    return ResponseEntity.noContent().build();
  }

  // Mark a goal as complete, creating a Win.
  @PostMapping("/{id}/complete")
  public ResponseEntity<String> completeGoal(@PathVariable Long id) {
    service.completeGoal(id);
    return ResponseEntity.ok("Goal completed and Win recorded.");
  }
}
