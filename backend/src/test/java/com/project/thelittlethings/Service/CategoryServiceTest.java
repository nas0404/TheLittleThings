// src/test/java/com/project/thelittlethings/services/CategoryServiceTest.java
package com.project.thelittlethings.Service;

import com.project.thelittlethings.dto.categories.*;
import com.project.thelittlethings.entities.*;
import com.project.thelittlethings.repositories.*;
import com.project.thelittlethings.services.CategoryService;

import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

  @Mock CategoryRepository categoryRepo;
  @Mock UserRepository userRepo;
  CategoryService service;

  @BeforeEach
  void setup() {
    MockitoAnnotations.openMocks(this);
    service = new CategoryService(categoryRepo, userRepo);
  }

  @Test
  void create_success() {
    var req = new CreateCategoryRequest(31L, "Fitness", "Gym");
    var user = new User(); user.setUserId(31L);

    when(userRepo.findById(31L)).thenReturn(Optional.of(user));
    when(categoryRepo.existsByUser_UserIdAndName(31L, "Fitness")).thenReturn(false);

    var saved = new Category();
    saved.setCategoryId(1L); saved.setUser(user);
    saved.setName("Fitness"); saved.setDescription("Gym");

    when(categoryRepo.save(any(Category.class))).thenReturn(saved);

    CategoryResponse res = service.create(req.getUserId(), req);

    assertEquals(1L, res.getCategoryId());
    assertEquals(31L, res.getUserId());
    assertEquals("Fitness", res.getName());
  }

  @Test
  void create_missingName_400() {
    var req = new CreateCategoryRequest(31L, "   ", null);
    var ex = assertThrows(IllegalArgumentException.class, () -> service.create(req.getUserId(), req));
    assertTrue(ex.getMessage().toLowerCase().contains("name is required"));
  }

  @Test
  void create_userNotFound_404() {
    var req = new CreateCategoryRequest(999L, "X", null);
    when(userRepo.findById(999L)).thenReturn(Optional.empty());
    var ex = assertThrows(IllegalArgumentException.class, () -> service.create(req.getUserId(), req));
    assertTrue(ex.getMessage().toLowerCase().contains("user not found"));
  }

  @Test
  void create_duplicate_409() {
    var req = new CreateCategoryRequest(31L, "Fitness", null);
    var user = new User(); user.setUserId(31L);
    when(userRepo.findById(31L)).thenReturn(Optional.of(user));
    when(categoryRepo.existsByUser_UserIdAndName(31L, "Fitness")).thenReturn(true);

    var ex = assertThrows(IllegalArgumentException.class, () -> service.create(req.getUserId(), req));
    assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
  }

  @Test
  void getOwned_wrongOwner_404() {
    var user = new User(); user.setUserId(31L);
    var cat = new Category(); cat.setCategoryId(9L); cat.setUser(user); cat.setName("A");
    when(categoryRepo.findById(9L)).thenReturn(Optional.of(cat));
    var ex = assertThrows(IllegalArgumentException.class, () -> service.getOwned(9L, 777L));
    assertTrue(ex.getMessage().toLowerCase().contains("not found"));
  }

  @Test
  void update_rename_conflict_409() {
    var user = new User(); user.setUserId(31L);
    var cat = new Category(); cat.setCategoryId(5L); cat.setUser(user); cat.setName("Old");
    when(categoryRepo.findById(5L)).thenReturn(Optional.of(cat));
    when(categoryRepo.existsByUser_UserIdAndName(31L, "New")).thenReturn(true);

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.update(5L, 31L, new UpdateCategoryRequest("New", null)));
    assertTrue(ex.getMessage().toLowerCase().contains("already exists"));
  }
}
