// src/test/java/com/project/thelittlethings/services/GoalServiceTest.java
package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.goals.CreateGoalRequest;
import com.project.thelittlethings.dto.goals.GoalResponse;
import com.project.thelittlethings.dto.goals.UpdateGoalRequest;
import com.project.thelittlethings.entities.Category;
import com.project.thelittlethings.entities.Goal;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.CategoryRepository;
import com.project.thelittlethings.repositories.GoalRepository;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.repositories.WinRepository;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GoalServiceTest {

  @Mock GoalRepository goalRepo;
  @Mock UserRepository userRepo;
  @Mock CategoryRepository categoryRepo;
  @Mock WinRepository winRepo;

  GoalService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new GoalService(goalRepo, userRepo, categoryRepo, winRepo);
  }

  // --- helpers -----------------------------------------------------

  private User CreateUserforGoal(long id) {
    var u = new User();
    u.setUserId(id);
    return u;
  }

  private Category CreateCategoryForGoal(long id, User owner) {
    var c = new Category();
    c.setCategoryId(id);
    c.setUser(owner);
    return c;
  }

  private void seedUserAndCategory(long userId, long catId) {
    var user = CreateUserforGoal(userId);
    var cat = CreateCategoryForGoal(catId, user);
    when(userRepo.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepo.findById(catId)).thenReturn(Optional.of(cat));
  }

  // --- tests -------------------------------------------------------

  @Test
  void create_success() {
    long userId = 31L, catId = 4L;

    var user = CreateUserforGoal(userId);
    var cat = CreateCategoryForGoal(catId, user);

    when(userRepo.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepo.findById(catId)).thenReturn(Optional.of(cat));

    var saved = new Goal();
    saved.setGoalId(100L);
    saved.setUser(user);
    saved.setCategory(cat);
    saved.setTitle("Learn to block");
    saved.setPriority("LOW");

    when(goalRepo.saveAndFlush(any(Goal.class))).thenReturn(saved);
    when(goalRepo.findById(100L)).thenReturn(Optional.of(saved));

    var req = new CreateGoalRequest(catId, "Learn to block", "hands up", "low");
    GoalResponse res = service.createGoal(userId, catId, req);

    assertEquals(100L, res.getGoalId());
    assertEquals(userId, res.getUserId());
    assertEquals(catId, res.getCategoryId());
    assertEquals("Learn to block", res.getTitle());
    assertEquals("LOW", res.getPriority());
  }

  @Test
  void create_missingTitle_400() {
    // Ensure we pass ownership checks and hit the title guard
    seedUserAndCategory(31L, 4L);

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.createGoal(31L, 4L, new CreateGoalRequest(4L, "   ", "x", "HIGH")));
    assertTrue(ex.getMessage().toLowerCase().contains("title is required"));
  }

  @Test
  void create_badPriority_400() {
    // Ensure we pass ownership checks and hit the priority guard
    seedUserAndCategory(31L, 4L);

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.createGoal(31L, 4L, new CreateGoalRequest(4L, "t", "x", "WRONG")));
    assertTrue(ex.getMessage().toLowerCase().contains("priority must be"));
  }

  @Test
  void create_userNotFound_404() {
    when(userRepo.findById(31L)).thenReturn(Optional.empty());
    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.createGoal(31L, 4L, new CreateGoalRequest(4L, "t", null, "HIGH")));
    assertTrue(ex.getMessage().toLowerCase().contains("user not found"));
  }

  @Test
  void create_categoryWrongOwner_400() {
    long userId = 31L, catId = 4L;

    var user = CreateUserforGoal(userId);
    var other = CreateUserforGoal(99L);
    var cat = CreateCategoryForGoal(catId, other);

    when(userRepo.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepo.findById(catId)).thenReturn(Optional.of(cat));

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.createGoal(userId, catId, new CreateGoalRequest(catId, "t", null, "HIGH")));
    assertTrue(ex.getMessage().toLowerCase().contains("does not belong"));
  }

  @Test
  void listByUser_success() {
    // mustUser() uses userRepo.findById(...)
    var user = CreateUserforGoal(31L);
    when(userRepo.findById(31L)).thenReturn(Optional.of(user));

    var cat = CreateCategoryForGoal(4L, user);

    var g1 = new Goal();
    g1.setGoalId(1L);
    g1.setUser(user);
    g1.setCategory(cat);
    g1.setTitle("A");
    g1.setPriority("HIGH");

    var g2 = new Goal();
    g2.setGoalId(2L);
    g2.setUser(user);
    g2.setCategory(cat);
    g2.setTitle("B");
    g2.setPriority("LOW");

    when(goalRepo.findByUser_UserId(31L)).thenReturn(List.of(g1, g2));

    var list = service.listGoalsByUser(31L);
    assertEquals(2, list.size());
    assertEquals("A", list.get(0).getTitle());
    assertEquals("B", list.get(1).getTitle());
  }

  @Test
  void getOwnedGoal_wrongOwner_404() {
    // mustGoalOwned() queries by goalId+userId together
    when(goalRepo.findByGoalIdAndUser_UserId(9L, 77L)).thenReturn(Optional.empty());

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.getOwnedGoal(9L, 77L));
    assertTrue(ex.getMessage().toLowerCase().contains("not found"));
  }

  @Test
  void update_priorityValidation_400() {
    var user = CreateUserforGoal(31L);
    var cat = CreateCategoryForGoal(4L, user);

    var goal = new Goal();
    goal.setGoalId(5L);
    goal.setUser(user);
    goal.setCategory(cat);
    goal.setTitle("Old");
    goal.setPriority("LOW");

    when(goalRepo.findByGoalIdAndUser_UserId(5L, 31L)).thenReturn(Optional.of(goal));

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.updateGoal(5L, 31L, new UpdateGoalRequest(null, null, "WRONG", null)));
    assertTrue(ex.getMessage().toLowerCase().contains("priority must be"));
  }

  @Test
  void delete_success() {
    var user = CreateUserforGoal(31L);
    var cat = CreateCategoryForGoal(4L, user);

    var goal = new Goal();
    goal.setGoalId(7L);
    goal.setUser(user);
    goal.setCategory(cat);

    when(goalRepo.findByGoalIdAndUser_UserId(7L, 31L)).thenReturn(Optional.of(goal));

    service.delete(7L, 31L);
    verify(goalRepo).delete(goal);
  }
}
