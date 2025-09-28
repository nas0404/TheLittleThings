package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.dto.goals.CreateGoalRequest;
import com.project.thelittlethings.dto.goals.GoalResponse;
import com.project.thelittlethings.dto.goals.UpdateGoalRequest;
import com.project.thelittlethings.services.CategoryService;
import com.project.thelittlethings.services.GoalService;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/users/{userId}/goals")
public class GoalController {

  private final GoalService service;

  public GoalController(GoalService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<GoalResponse> create(@PathVariable Long userId,
      @RequestBody CreateGoalRequest req,
      UriComponentsBuilder uri) {
    var created = service.createGoal(userId, req.getCategoryId(), req);
    var location = uri.path("/api/users/{userId}/goals/{id}")
        .buildAndExpand(userId, created.getGoalId())
        .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping
  public List<GoalResponse> list(@PathVariable Long userId,
      @RequestParam(required = false) Long categoryId) {
    return (categoryId == null)
        ? service.listGoalsByUser(userId)
        : service.listGoalsByUserAndCategory(userId, categoryId);
  }

  @GetMapping("/grouped")
  public Map<String, List<GoalResponse>> listGrouped(@PathVariable Long userId,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String priority) {
    return service.listGrouped(userId, categoryId, priority);
  }

  @GetMapping("/{goalId}")
  public GoalResponse getOne(@PathVariable Long userId, @PathVariable Long goalId) {
    return service.getOwnedGoal(goalId, userId);
  }

  @PutMapping("/{goalId}")
  public ResponseEntity<GoalResponse> update(@PathVariable Long userId,
      @PathVariable Long goalId,
      @RequestBody UpdateGoalRequest req) {
    return ResponseEntity.ok(service.updateGoal(goalId, userId, req));
  }

  @DeleteMapping("/{goalId}")
  public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long goalId) {
    service.delete(goalId, userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/complete")
  public ResponseEntity<String> completeGoal(@PathVariable Long id) {
    service.completeGoal(id);
    return ResponseEntity.ok("Goal completed and Win recorded.");
  }
}
