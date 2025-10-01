package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, Long> {

    List<Goal> findByUser_UserId(Long userId);

    List<Goal> findByUser_UserIdAndCategory_CategoryId(Long userId, Long categoryId);

    Optional<Goal> findByGoalIdAndUser_UserId(Long goalId, Long userId);

    boolean existsByGoalIdAndUser_UserId(Long goalId, Long userId);
    boolean existsByUser_UserId(Long userId);
    boolean existsByCategory_CategoryId(Long categoryId);
    boolean existsByUser_UserIdAndTitle(Long userId, String title); 

    long countByUser_UserId(Long userId);
    long countByCategory_CategoryId(Long categoryId);

    // ---- Sorting by priority (HIGH > MEDIUM > LOW) ----
    // return sorted results by priority weight:
    List<Goal> findByUser_UserIdAndPriorityOrderByCreatedAtDesc(Long userId, String priority);
     List<Goal> findByUser_UserIdAndCategory_CategoryIdAndPriorityOrderByCreatedAtDesc(Long userId, Long categoryId, String priority);
}

