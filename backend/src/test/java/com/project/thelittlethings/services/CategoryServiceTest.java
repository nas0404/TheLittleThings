// src/test/java/com/project/thelittlethings/services/CategoryServiceTest.java
package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.categories.CreateCategoryRequest;
import com.project.thelittlethings.dto.categories.UpdateCategoryRequest;
import com.project.thelittlethings.entities.Category;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.CategoryRepository;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.services.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

  private CategoryRepository categoryRepo;
  private UserRepository userRepo;
  private CategoryService service;

  @BeforeEach
  void setup() {
    categoryRepo = mock(CategoryRepository.class);
    userRepo = mock(UserRepository.class);
    MockitoAnnotations.openMocks(this);
    service = new CategoryService(categoryRepo, userRepo);
  }

  @Test
  void create_success() {
    long userId = 31L;
    var user = new User();
    user.setUserId(userId);

    when(userRepo.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepo.existsByUser_UserIdAndName(userId, "Fitness")).thenReturn(false);

    var saved = new Category();
    saved.setCategoryId(1L);
    saved.setUser(user);
    saved.setName("Fitness");

    when(categoryRepo.save(any(Category.class))).thenReturn(saved);
    when(categoryRepo.findById(1L)).thenReturn(Optional.of(saved)); // re-read after save

    var res = service.create(userId, new CreateCategoryRequest("Fitness", "Gym"));
    assertEquals(1L, res.getCategoryId());
  }

  @Test
  void create_missingName_400() {
    var req = new CreateCategoryRequest("   ", null);
    var ex = assertThrows(IllegalArgumentException.class, () -> service.create(1L, req));
    assertTrue(ex.getMessage().toLowerCase().contains("name is required"));
  }

  @Test
  void create_userNotFound_404() {
    var req = new CreateCategoryRequest("X", null);
    when(userRepo.findById(999L)).thenReturn(Optional.empty());

    var ex = assertThrows(IllegalArgumentException.class, () -> service.create(999L, req));
    assertTrue(ex.getMessage().toLowerCase().contains("user not found"));
  }

  @Test
  void create_duplicate_409() {
    var req = new CreateCategoryRequest("Fitness", null);
    var user = new User();
    user.setUserId(31L);

    when(userRepo.findById(31L)).thenReturn(Optional.of(user));
    when(categoryRepo.existsByUser_UserIdAndName(31L, "Fitness")).thenReturn(true);

    var ex = assertThrows(IllegalArgumentException.class, () -> service.create(31L, req));
    assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
  }

  @Test
  void getOwned_wrongOwner_404() {
    var owner = new User();
    owner.setUserId(31L);

    var cat = new Category();
    cat.setCategoryId(9L);
    cat.setUser(owner);
    cat.setName("A");

    when(categoryRepo.findById(9L)).thenReturn(Optional.of(cat));

    var ex = assertThrows(IllegalArgumentException.class, () -> service.getOwned(9L, 777L));
    assertTrue(ex.getMessage().toLowerCase().contains("not found"));
  }

  @Test
  void update_rename_conflict_409() {
    var owner = new User();
    owner.setUserId(31L);

    var cat = new Category();
    cat.setCategoryId(5L);
    cat.setUser(owner);
    cat.setName("Old");

    when(categoryRepo.findById(5L)).thenReturn(Optional.of(cat));
    when(categoryRepo.existsByUser_UserIdAndName(31L, "New")).thenReturn(true);

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.update(5L, 31L, new UpdateCategoryRequest("New", null)));
    assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
  }
}
