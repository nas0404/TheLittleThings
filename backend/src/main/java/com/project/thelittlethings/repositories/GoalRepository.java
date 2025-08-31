package com.project.thelittlethings.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.project.thelittlethings.entities.Goal;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    //  Listing
    List<Goal> findByUserId(Long userId);                // all goals for a user
    List<Goal> findByUserIdAndCategoryId(Long userId, Long categoryId);    // all goals in a category

    // Safe ownership fetch
    Optional<Goal> findByGoalIdAndUserId(Long goalId, Long userId);

    // Boolean checks
    boolean existsByGoalIdAndUserId(Long goalId, Long userId);   // ownership check
    boolean existsByUserId(Long userId);                         // any goals for user?
    boolean existsByCategoryId(Long categoryId);              // any goals in category?
    boolean existsByUserIdAndTitle(Long userId, String title);   // duplicate title check

    // Optional counters
    long countByUserId(Long userId);
    long countByCategoryId(Long categoryId);
}

