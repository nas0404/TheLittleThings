package com.project.thelittlethings.services;

import java.util.HashMap;
import java.util.List;
import java.util.Locale.Category;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.dto.goals.CreateGoalRequest;
import com.project.thelittlethings.dto.goals.GoalResponse;
import com.project.thelittlethings.dto.goals.UpdateGoalRequest;
import com.project.thelittlethings.entities.Goal;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.CategoryRepository;
import com.project.thelittlethings.repositories.GoalRepository;
import com.project.thelittlethings.repositories.UserRepository;

@Service
public class GoalService {

  private final GoalRepository goalRepo;
  private final UserRepository userRepo;
  private final CategoryRepository categoryRepo;

  public GoalService(GoalRepository g, UserRepository u, CategoryRepository c) {
    this.goalRepo = g; this.userRepo = u; this.categoryRepo = c;
  }
  private GoalResponse toResponse(Goal save) {
        // TODO Auto-generated method stub
        var r = new GoalResponse();
        r.setGoalId(save.getGoalId());
        r.setUserId(save.getUser().getUserId());
        r.setCategoryId(save.getCategory().getCategoryId());
        r.setTitle(save.getTitle());
        r.setDescription(save.getDescription());
        r.setPriority(save.getPriority());
        r.setCreatedAt(save.getCreatedAt());
        return r;
    }
    public GoalResponse createGoal(CreateGoalRequest gr){
       if(gr.getUserId() == null) throw new IllegalArgumentException("userId is required");
       if(gr.getCategoryId() == null) throw new IllegalArgumentException("categoryId is required");
       if(gr.getTitle() == null || gr.getTitle().trim().isEmpty()) throw new IllegalArgumentException("title is required");
       if(gr.getPriority() == null || gr.getPriority().trim().isEmpty()) throw new IllegalArgumentException("priority is required");
       if(!gr.getPriority().equals("HIGH") && !gr.getPriority().equals("MEDIUM") && !gr.getPriority().equals("LOW")) throw new IllegalArgumentException("priority must be HIGH, MEDIUM, or LOW");

         var user = userRepo.findById(gr.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("user not found"));
        var category = categoryRepo.findById(gr.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("category not found"));
        var goal = new Goal();
        goal.setUser(user);
        goal.setCategory(category);
        goal.setTitle(gr.getTitle().trim());
        goal.setDescription(gr.getDescription());
        goal.setPriority(gr.getPriority().trim());
        return toResponse(goalRepo.save(goal));
    }
    public List<GoalResponse> listGoalsByUser(long userId) {
    if (!userRepo.existsById(userId))
      throw new IllegalArgumentException("user not found");

    return goalRepo.findByUser_UserId(userId)
        .stream()
        .map(this::toResponse)
        .toList();
  }
  public List<GoalResponse> listGoalsByUserAndCategory(long userId, long categoryId) {
    if (!userRepo.existsById(userId) || !categoryRepo.existsById(categoryId))
      throw new IllegalArgumentException("user or category not found");

    return goalRepo.findByUser_UserIdAndCategory_CategoryId(userId, categoryId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public Map<String, List<GoalResponse>> listGrouped(Long userId, Long categoryId) {
    List<Goal> all = (categoryId == null)
        ? goalRepo.findByUser_UserId(userId)
        : goalRepo.findByUser_UserIdAndCategory_CategoryId(userId, categoryId);

    Map<String, List<GoalResponse>> grouped = new HashMap<>();
    grouped.put("HIGH", all.stream().filter(g -> "HIGH".equals(g.getPriority())).map(this::toResponse).toList());
    grouped.put("MEDIUM", all.stream().filter(g -> "MEDIUM".equals(g.getPriority())).map(this::toResponse).toList());
    grouped.put("LOW", all.stream().filter(g -> "LOW".equals(g.getPriority())).map(this::toResponse).toList());

    return grouped;
}
  public GoalResponse getOwnedGoal(Long goalId, Long userId) {
    Goal g = goalRepo.findById(goalId)
        .orElseThrow(() -> new IllegalArgumentException("goal not found"));
    if (!g.getUser().getUserId().equals(userId))
      throw new IllegalArgumentException("not found");
      return toResponse(g);
  }
  public GoalResponse updateGoal(Long goalId, Long userId, UpdateGoalRequest r) {
   var g = goalRepo.findByGoalIdAndUser_UserId(goalId, userId)
        .orElseThrow(() -> new IllegalArgumentException("not found"));
        if(r.getTitle() != null){
          String newTitle = r.getTitle().trim();
          if(newTitle.isEmpty()) throw new IllegalArgumentException("title cannot be blank");
          if(!newTitle.equals(g.getTitle()) && goalRepo.existsByUser_UserIdAndTitle(userId, newTitle))
            throw new IllegalArgumentException("goal title already exists for this user");
            g.setTitle(newTitle);
        }
        if (r.getDescription()!=null) g.setDescription(r.getDescription());
        if (r.getPriority()!=null) {
        var p = r.getPriority().toUpperCase();
        if (!p.equals("HIGH") && !p.equals("MEDIUM") && !p.equals("LOW"))
            throw new IllegalArgumentException("priority must be HIGH, MEDIUM or LOW");
             g.setPriority(p);
    }
    if (r.getCategoryId()!=null) {
      var cat = categoryRepo.findById(r.getCategoryId())
          .orElseThrow(() -> new IllegalArgumentException("category not found"));
      if (!cat.getUser().getUserId().equals(userId))
        throw new IllegalArgumentException("category does not belong to user");
      g.setCategory(cat);
    }
    return toResponse(goalRepo.save(g));
    
  }
  public void delete(Long goalId, Long userId) {
    var g = goalRepo.findByGoalIdAndUser_UserId(goalId, userId)
        .orElseThrow(() -> new IllegalArgumentException("not found"));
    goalRepo.delete(g);
  }
}

