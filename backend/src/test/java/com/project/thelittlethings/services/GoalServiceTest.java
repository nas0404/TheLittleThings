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
import com.project.thelittlethings.services.GoalService;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GoalServiceTest {

  @Mock
  GoalRepository goalRepo;
  @Mock
  UserRepository userRepo;
  @Mock
  CategoryRepository categoryRepo;
  @Mock
  WinRepository winRepo;
  GoalService service;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    service = new GoalService(goalRepo, userRepo, categoryRepo, winRepo);
  }

  @Test
  void create_success() {
    Long userId = 31L, catId = 4L;

    var user = new User();
    user.setUserId(userId);
    var cat = new Category();
    cat.setCategoryId(catId);
    cat.setUser(user);

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
    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.createGoal(31L, 4L, new CreateGoalRequest(4L, "   ", "x", "HIGH")));
    assertTrue(ex.getMessage().toLowerCase().contains("title is required"));
  }

  @Test
  void create_badPriority_400() {
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
    Long userId = 31L, catId = 4L;
    var user = new User();
    user.setUserId(userId);

    var otherUser = new User();
    otherUser.setUserId(99L);
    var cat = new Category();
    cat.setCategoryId(catId);
    cat.setUser(otherUser);

    when(userRepo.findById(userId)).thenReturn(Optional.of(user));
    when(categoryRepo.findById(catId)).thenReturn(Optional.of(cat));

    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.createGoal(userId, catId, new CreateGoalRequest(catId, "t", null, "HIGH")));
    assertTrue(ex.getMessage().toLowerCase().contains("does not belong"));
  }

  @Test
  void listByUser_success() {
    when(userRepo.existsById(31L)).thenReturn(true);

    var user = new User();
    user.setUserId(31L);
    var cat = new Category();
    cat.setCategoryId(4L);
    cat.setUser(user);

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
  }

  @Test
  void getOwnedGoal_wrongOwner_404() {
    var owner = new User();
    owner.setUserId(31L);
    var other = new User();
    other.setUserId(77L);
    var cat = new Category();
    cat.setCategoryId(4L);
    cat.setUser(owner);

    var goal = new Goal();
    goal.setGoalId(9L);
    goal.setUser(owner);
    goal.setCategory(cat);
    goal.setTitle("t");
    when(goalRepo.findById(9L)).thenReturn(Optional.of(goal));

    var ex = assertThrows(IllegalArgumentException.class, () -> service.getOwnedGoal(9L, 77L));
    assertTrue(ex.getMessage().toLowerCase().contains("not found"));
  }

  @Test
  void update_priorityValidation_400() {
    var user = new User();
    user.setUserId(31L);
    var cat = new Category();
    cat.setCategoryId(4L);
    cat.setUser(user);

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
    var user = new User();
    user.setUserId(31L);
    var cat = new Category();
    cat.setCategoryId(4L);
    cat.setUser(user);
    var goal = new Goal();
    goal.setGoalId(7L);
    goal.setUser(user);
    goal.setCategory(cat);

    when(goalRepo.findByGoalIdAndUser_UserId(7L, 31L)).thenReturn(Optional.of(goal));

    service.delete(7L, 31L);
    verify(goalRepo).delete(goal);
  }
}
