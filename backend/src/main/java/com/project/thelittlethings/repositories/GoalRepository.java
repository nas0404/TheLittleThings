package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

// Repository interface for Goal entity providing CRUD operations and custom queries
public interface GoalRepository extends JpaRepository<Goal, Long> {

    // Find all goals belonging to a specific user
    List<Goal> findByUser_UserId(Long userId);

    // Find all goals belonging to a specific user and category
    List<Goal> findByUser_UserIdAndCategory_CategoryId(Long userId, Long categoryId);

    // Find a specific goal by its ID and owner user ID
    Optional<Goal> findByGoalIdAndUser_UserId(Long goalId, Long userId);

    // Check if a goal exists for a specific user
    boolean existsByGoalIdAndUser_UserId(Long goalId, Long userId);
    // Check if a user has any goals
    boolean existsByUser_UserId(Long userId);
    // Check if a category has any goals
    boolean existsByCategory_CategoryId(Long categoryId);
    // Check if a user already has a goal with the given title
    boolean existsByUser_UserIdAndTitle(Long userId, String title); 

    // Count total number of goals for a user
    long countByUser_UserId(Long userId);
    // Count total number of goals in a category
    long countByCategory_CategoryId(Long categoryId);

    // ---- Sorting by priority (HIGH > MEDIUM > LOW) ----
    // Find goals by user and priority, sorted by creation date (newest first)
    List<Goal> findByUser_UserIdAndPriorityOrderByCreatedAtDesc(Long userId, String priority);
    // Find goals by user, category and priority, sorted by creation date (newest first)
    List<Goal> findByUser_UserIdAndCategory_CategoryIdAndPriorityOrderByCreatedAtDesc(Long userId, Long categoryId, String priority);
}

