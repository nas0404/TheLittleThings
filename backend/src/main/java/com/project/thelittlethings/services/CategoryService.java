package com.project.thelittlethings.services;

import com.project.thelittlethings.MaterialisedView.CategoryNeglectedView;
import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.dto.categories.CreateCategoryRequest;
import com.project.thelittlethings.dto.categories.UpdateCategoryRequest;
import com.project.thelittlethings.entities.Category;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.CategoryRepository;
import com.project.thelittlethings.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

  private final CategoryRepository categoryRepo;
  private final UserRepository userRepo;

  public CategoryService(CategoryRepository categoryRepo, UserRepository userRepo) {
    this.categoryRepo = categoryRepo;
    this.userRepo = userRepo;
  }

  

  public CategoryResponse create(Long userId, CreateCategoryRequest r) {
    if (userId == null) throw new IllegalArgumentException("userId is required");
    if (r.getName() == null || r.getName().trim().isEmpty())
      throw new IllegalArgumentException("name is required");

    User user = userRepo.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("user not found"));

    String name = r.getName().trim();
    if (categoryRepo.existsByUser_UserIdAndName(user.getUserId(), name))
      throw new IllegalArgumentException("category name already exists for this user");

    Category c = new Category();
    c.setUser(user);
    c.setName(name);
    c.setDescription(r.getDescription());

    Category saved = categoryRepo.save(c);
    // re-read to populate DB-managed timestamps
    saved = categoryRepo.findById(saved.getCategoryId())
        .orElseThrow(() -> new IllegalArgumentException("category missing after save"));
    return toResponse(saved);
  }


  public List<CategoryResponse> listByUser(long userId) {
    if (!userRepo.existsById(userId))
      throw new IllegalArgumentException("user not found");

    return categoryRepo.findByUser_UserId(userId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public CategoryResponse getOwned(Long categoryId, Long userId) {
    Category c = categoryRepo.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("category not found"));
    if (!c.getUser().getUserId().equals(userId))
      throw new IllegalArgumentException("not found");
    return toResponse(c);
  }

  public CategoryResponse update(Long categoryId, Long userId, UpdateCategoryRequest r) {
    Category c = categoryRepo.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("category not found"));
    if (!c.getUser().getUserId().equals(userId))
      throw new IllegalArgumentException("not found");

    if (r.getName() != null) {
      String newName = r.getName().trim();
      if (newName.isEmpty())
        throw new IllegalArgumentException("name cannot be blank");
      if (!newName.equals(c.getName())
          && categoryRepo.existsByUser_UserIdAndName(userId, newName))
        throw new IllegalArgumentException("category name already exists for this user");
      c.setName(newName);
    }
    if (r.getDescription() != null)
      c.setDescription(r.getDescription());

    return toResponse(categoryRepo.save(c));
  }

  public void delete(Long categoryId, Long userId) {
    Category c = categoryRepo.findById(categoryId)
        .orElseThrow(() -> new IllegalArgumentException("category not found"));
    if (!c.getUser().getUserId().equals(userId))
      throw new IllegalArgumentException("not found");
    categoryRepo.delete(c);
  }

  private CategoryResponse toResponse(Category c) {
    CategoryResponse res = new CategoryResponse();
    res.setCategoryId(c.getCategoryId());
    res.setUserId(c.getUser().getUserId());
    res.setName(c.getName());
    res.setDescription(c.getDescription());
    res.setCreatedAt(c.getCreatedAt());
    res.setUpdatedAt(c.getUpdatedAt());
    return res;
  }

  public List<CategoryNeglectedView> getNeglectedCategories(Long userId, Integer days) {
    int lookback = (days == null || days < 1) ? 14 : days;
    return categoryRepo.findNeglectedCategories(userId, lookback);
  }

}
