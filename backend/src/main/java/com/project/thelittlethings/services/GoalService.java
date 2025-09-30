package com.project.thelittlethings.services;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.thelittlethings.dto.goals.CreateGoalRequest;
import com.project.thelittlethings.dto.goals.GoalResponse;
import com.project.thelittlethings.dto.goals.UpdateGoalRequest;
import com.project.thelittlethings.entities.Category;
import com.project.thelittlethings.entities.Goal;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.entities.Win;
import com.project.thelittlethings.repositories.CategoryRepository;
import com.project.thelittlethings.repositories.GoalRepository;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.repositories.WinRepository;

@Service
@Transactional
public class GoalService {

  private final GoalRepository goalRepo;
  private final UserRepository userRepo;
  private final CategoryRepository categoryRepo;
  private final WinRepository winRepo;

  private static final boolean ENFORCE_UNIQUE_TITLES_PER_USER = false;

  public GoalService(GoalRepository g, UserRepository u, CategoryRepository c, WinRepository w) {
    this.goalRepo = g;
    this.userRepo = u;
    this.categoryRepo = c;
    this.winRepo = w;
  }


  private static String trimOrNull(String s) { return s == null ? null : s.trim(); }

  private static String normPriority(String p) { return p == null ? null : p.trim().toUpperCase(); }

  private static void requirePriority(String p) {
    String v = normPriority(p);
    if (v == null || v.isEmpty()) throw new IllegalArgumentException("priority is required");
    if (!v.equals("HIGH") && !v.equals("MEDIUM") && !v.equals("LOW"))
      throw new IllegalArgumentException("priority must be HIGH, MEDIUM, or LOW");
  }

  private User mustUser(Long userId) {
    return userRepo.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("user not found"));
  }

  private Category mustCategoryOwned(Long userId, Long categoryId) {
    if (categoryId == null) throw new IllegalArgumentException("categoryId is required");
    Category c = categoryRepo.findById(categoryId)
      .orElseThrow(() -> new IllegalArgumentException("category not found"));
    if (!Objects.equals(c.getUser().getUserId(), userId))
      throw new IllegalArgumentException("category does not belong to user");
    return c;
  }

  private Goal mustGoalOwned(Long goalId, Long userId) {
    return goalRepo.findByGoalIdAndUser_UserId(goalId, userId)
      .orElseThrow(() -> new IllegalArgumentException("goal not found"));
  }

  private GoalResponse toResponse(Goal g) {
    GoalResponse r = new GoalResponse();
    r.setGoalId(g.getGoalId());
    r.setUserId(g.getUser().getUserId());
    r.setCategoryId(g.getCategory() != null ? g.getCategory().getCategoryId() : null);
    r.setTitle(g.getTitle());
    r.setDescription(g.getDescription());
    r.setPriority(g.getPriority());
    r.setCreatedAt(g.getCreatedAt());
    r.setUpdatedAt(g.getUpdatedAt());
    return r;
  }


  public GoalResponse create(Long userId, CreateGoalRequest req) {
    if (userId == null) throw new IllegalArgumentException("userId is required");

    User user = mustUser(userId);
    Category category = mustCategoryOwned(userId, req.getCategoryId());

    String title = trimOrNull(req.getTitle());
    if (title == null || title.isEmpty())
      throw new IllegalArgumentException("title is required");
    if (title.length() > 255)
      throw new IllegalArgumentException("title must be ≤ 255 characters");

    String description = trimOrNull(req.getDescription());
    if (description != null && description.length() > 100)
      throw new IllegalArgumentException("description must be ≤ 100 characters");

    String priority = normPriority(req.getPriority());
    requirePriority(priority);

    if (ENFORCE_UNIQUE_TITLES_PER_USER && goalRepo.existsByUser_UserIdAndTitle(userId, title))
      throw new IllegalArgumentException("goal title already exists for this user");

    Goal g = new Goal();
    g.setUser(user);
    g.setCategory(category);
    g.setTitle(title);
    g.setDescription(description);
    g.setPriority(priority);

    Goal saved = goalRepo.save(g);
    return toResponse(saved);
  }

  @Transactional(readOnly = true)
  public List<GoalResponse> listGoalsByUser(long userId) {
    mustUser(userId);
    return goalRepo.findByUser_UserId(userId).stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<GoalResponse> listGoalsByUserAndCategory(long userId, long categoryId) {
    mustUser(userId);
    mustCategoryOwned(userId, categoryId);
    return goalRepo.findByUser_UserIdAndCategory_CategoryId(userId, categoryId)
                   .stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public Map<String, List<GoalResponse>> listGrouped(Long userId, Long categoryId, String priority) {
    mustUser(userId);
    if (categoryId != null) mustCategoryOwned(userId, categoryId);

    List<Goal> all = (categoryId == null)
        ? goalRepo.findByUser_UserId(userId)
        : goalRepo.findByUser_UserIdAndCategory_CategoryId(userId, categoryId);

    Map<String, List<GoalResponse>> grouped = new HashMap<>();
    grouped.put("HIGH", all.stream().filter(g -> "HIGH".equalsIgnoreCase(g.getPriority()))
        .map(this::toResponse).toList());
    grouped.put("MEDIUM", all.stream().filter(g -> "MEDIUM".equalsIgnoreCase(g.getPriority()))
        .map(this::toResponse).toList());
    grouped.put("LOW", all.stream().filter(g -> "LOW".equalsIgnoreCase(g.getPriority()))
        .map(this::toResponse).toList());

    if (priority != null) {
      String p = normPriority(priority);
      requirePriority(p);
      return Map.of(p, grouped.get(p));
    }
    return grouped;
  }

  @Transactional(readOnly = true)
  public GoalResponse getOwnedGoal(Long goalId, Long userId) {
    return toResponse(mustGoalOwned(goalId, userId));
  }

  public void completeGoal(Long goalId) {
    Goal goal = goalRepo.findById(goalId)
        .orElseThrow(() -> new RuntimeException("Goal not found"));

    Win win = new Win();
    win.setGoal(goal);
    win.setUser(goal.getUser());
    win.setTitle(goal.getTitle());
    win.setDescription(goal.getDescription());
    win.setCompletionDate(OffsetDateTime.now());
    win.setNumTrophies(1); 
    winRepo.save(win);
  }

  public GoalResponse updateGoal(Long goalId, Long userId, UpdateGoalRequest r) {
    Goal g = mustGoalOwned(goalId, userId);

    if (r.getTitle() != null) {
      String newTitle = trimOrNull(r.getTitle());
      if (newTitle == null || newTitle.isEmpty())
        throw new IllegalArgumentException("title cannot be blank");
      if (newTitle.length() > 255)
        throw new IllegalArgumentException("title must be ≤ 255 characters");
      if (ENFORCE_UNIQUE_TITLES_PER_USER
          && !newTitle.equals(g.getTitle())
          && goalRepo.existsByUser_UserIdAndTitle(userId, newTitle)) {
        throw new IllegalArgumentException("goal title already exists for this user");
      }
      g.setTitle(newTitle);
    }

    if (r.getDescription() != null) {
      String d = trimOrNull(r.getDescription());
      if (d != null && d.length() > 100)
        throw new IllegalArgumentException("description must be ≤ 100 characters");
      g.setDescription(d);
    }

    if (r.getPriority() != null) {
      String p = normPriority(r.getPriority());
      requirePriority(p);
      g.setPriority(p);
    }

    if (r.getCategoryId() != null) {
      Category cat = mustCategoryOwned(userId, r.getCategoryId());
      g.setCategory(cat);
    }

    return toResponse(goalRepo.save(g));
  }

  public void delete(Long goalId, Long userId) {
    Goal g = mustGoalOwned(goalId, userId);
    goalRepo.delete(g);
  }
}
