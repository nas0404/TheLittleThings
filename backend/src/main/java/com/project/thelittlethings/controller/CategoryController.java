package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.dto.categories.CreateCategoryRequest;
import com.project.thelittlethings.dto.categories.UpdateCategoryRequest;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.security.HMACtokens;
import com.project.thelittlethings.services.CategoryService;
import com.project.thelittlethings.services.UserService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService categoryService;
  private final UserService userService;

  public CategoryController(CategoryService categoryService, UserService userService) {
    this.categoryService = categoryService;
    this.userService = userService;
  }

  private Long userIdFromAuth(String authHeader) {
    final String token = authHeader != null && authHeader.startsWith("Bearer ")
        ? authHeader.substring(7)
        : authHeader;

    if (!HMACtokens.validateToken(token)) {
      throw new IllegalArgumentException("Invalid or expired token");
    }
    final String username = HMACtokens.extractUsername(token);
    if (username == null) throw new IllegalArgumentException("Invalid token");

    User u = userService.findByUsername(username);
    if (u == null) throw new IllegalArgumentException("User not found");
    return u.getUserId();
  }


  @GetMapping
  public ResponseEntity<?> list(@RequestHeader("Authorization") String auth) {
    try {
      Long userId = userIdFromAuth(auth);
      List<CategoryResponse> categories = categoryService.listByUser(userId);
      return ResponseEntity.ok(categories);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @PostMapping
  public ResponseEntity<?> create(@RequestHeader("Authorization") String auth,
                                  @Valid @RequestBody CreateCategoryRequest req) {
    try {
      Long userId = userIdFromAuth(auth);
      CategoryResponse created = categoryService.create(userId, req);
      return ResponseEntity
          .created(URI.create("/api/categories/" + created.getCategoryId()))
          .body(created);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> get(@RequestHeader("Authorization") String auth,
                               @PathVariable("id") Long id) {
    try {
      Long userId = userIdFromAuth(auth);
      CategoryResponse cat = categoryService.getOwned(id, userId);
      return ResponseEntity.ok(cat);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@RequestHeader("Authorization") String auth,
                                  @PathVariable("id") Long id,
                                  @Valid @RequestBody UpdateCategoryRequest req) {
    try {
      Long userId = userIdFromAuth(auth);
      CategoryResponse updated = categoryService.update(id, userId, req);
      return ResponseEntity.ok(updated);
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@RequestHeader("Authorization") String auth,
                                  @PathVariable("id") Long id) {
    try {
      Long userId = userIdFromAuth(auth);
      categoryService.delete(id, userId);
      return ResponseEntity.noContent().build();
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

  @GetMapping("/neglected")
  public ResponseEntity<?> neglected(@RequestHeader("Authorization") String auth,
                                     @RequestParam(value = "days", required = false) Integer days) {
    try {
      Long userId = userIdFromAuth(auth);
      return ResponseEntity.ok(categoryService.getNeglectedCategories(userId, days));
    } catch (IllegalArgumentException ex) {
      return ResponseEntity.badRequest().body(ex.getMessage());
    }
  }

//   @PostMapping("/{id}/complete")
//   public ResponseEntity<String> completeGoal(@PathVariable Long id) {
//     service.completeGoal(id);
//     return ResponseEntity.ok("Goal completed and Win recorded.");
//   }
}
