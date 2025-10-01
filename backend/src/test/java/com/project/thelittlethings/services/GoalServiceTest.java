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

 @Test
  void create_success() {
    Long userId = 7L;

    CreateGoalRequest req = new CreateGoalRequest();
    req.setCategoryId(11L);
    req.setTitle("Cut body fat");
    req.setDescription("12-week plan");
    req.setPriority("HIGH");

    User user = new User(); user.setUserId(userId);
    when(userRepo.findById(userId)).thenReturn(Optional.of(user));

    Category cat = new Category(); cat.setCategoryId(11L); cat.setUser(user);
    when(categoryRepo.findById(11L)).thenReturn(Optional.of(cat));

    when(goalRepo.existsByUser_UserIdAndTitle(userId, "Cut body fat")).thenReturn(false);

    when(goalRepo.save(any(Goal.class))).thenAnswer(inv -> {
      Goal g = inv.getArgument(0);
      g.setGoalId(123L);
      return g;
    });

    GoalResponse res = service.create(userId, req);

    assertNotNull(res);
    assertEquals(123L, res.getGoalId());
    assertEquals("Cut body fat", res.getTitle());
    assertEquals("HIGH", res.getPriority());
  }


  @Test
  void create_userNotFound_404() {
    when(userRepo.findById(31L)).thenReturn(Optional.empty());
    var ex = assertThrows(IllegalArgumentException.class,
        () -> service.create(31L, new CreateGoalRequest(4L, "t", null, "HIGH")));
    assertTrue(ex.getMessage().toLowerCase().contains("user not found"));
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
