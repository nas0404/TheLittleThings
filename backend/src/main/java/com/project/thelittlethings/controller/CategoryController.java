package com.project.thelittlethings.controller;


import com.project.thelittlethings.MaterialisedView.CategoryNeglectedView;
import com.project.thelittlethings.View.CategoryNeglectView;
import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.dto.categories.CreateCategoryRequest;
import com.project.thelittlethings.dto.categories.UpdateCategoryRequest;
import com.project.thelittlethings.services.CategoryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/categories")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // Create
  @PostMapping
  public ResponseEntity<CategoryResponse> create(@PathVariable Long userId,
                                                 @Valid @RequestBody CreateCategoryRequest req,
                                                 UriComponentsBuilder uri) {
    CategoryResponse created = service.create(userId, req);
    URI location = uri.path("/api/users/{userId}/categories/{id}").buildAndExpand(userId, created.getCategoryId()).toUri();
    return ResponseEntity.created(location).body(created);
  }

    // List all categories for a users
    @GetMapping
    public List<CategoryResponse> list(@PathVariable Long userId) {
        return service.listByUser(userId);
    }

    // Get single category
    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long userId,
                                @PathVariable Long id) {
        return service.getOwned(id, userId);
    }

    // Update 
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long userId,
                                                   @Valid @PathVariable Long id,
                                                   @RequestBody UpdateCategoryRequest req) {
        return ResponseEntity.ok(service.update(id, userId, req));
    }

    // Delete a category 
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long userId, @PathVariable Long id) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    // Get neglected categories (no wins in X days, default 14)
    // Validations also put in place for days param
    @GetMapping("/neglected")
    public List<CategoryNeglectedView> neglected(
        @PathVariable @Positive(message = "userId must be a positive number") Long userId,
        @RequestParam(defaultValue = "14")
        @Min(value = 1, message = "days must be at least 1")
        @Max(value = 3650, message = "days must be ≤ 3650") Integer days) {
        return service.getNeglectedCategories(userId, days);
    }
}


