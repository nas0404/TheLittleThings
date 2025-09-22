package com.project.thelittlethings.controller;

import com.project.thelittlethings.MaterialisedView.CategoryNeglectedView;
import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.dto.categories.CreateCategoryRequest;
import com.project.thelittlethings.dto.categories.UpdateCategoryRequest;
import com.project.thelittlethings.services.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/{userId}/categories/")
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    // Create
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@RequestBody CreateCategoryRequest req) {
        return ResponseEntity.ok(service.create(req));
    }

    // List all categories for a user
    @GetMapping
    public List<CategoryResponse> list(@RequestParam Long userId) {
        return service.listByUser(userId);
    }

    // Get single category (ownership check)
    @GetMapping("/{id}")
    public CategoryResponse get(@PathVariable Long id, @RequestParam Long userId) {
        return service.getOwned(id, userId);
    }

    // Update
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @RequestParam Long userId,
                                                   @RequestBody UpdateCategoryRequest req) {
        return ResponseEntity.ok(service.update(id, userId, req));
    }

    // Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
        service.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories/neglected")
    public List<CategoryNeglectedView> neglected(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "30") int days
    ) 
    {
        return service.getNeglectedCategories(userId, days);
    }
}

