package com.project.thelittlethings.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.thelittlethings.MaterialisedView.CategoryNeglectedView;
import com.project.thelittlethings.entities.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find all categories that belong to a specific user (by userId).
    List<Category> findByUser_UserId(Long userId);
    boolean existsByUser_UserIdAndName(Long userId, String name);

    // Check if this user has ANY categories
    boolean existsByUser_UserId(Long userId);

      @Query(value = """
    WITH last_win AS (
      SELECT g.category_id, MAX(w.created_at) AS last_win_at
      FROM goals g
      LEFT JOIN wins w ON w.goal_id = g.goal_id
      WHERE g.user_id = :userId
      GROUP BY g.category_id
    )
    SELECT
      c.category_id AS categoryId,
      c.name        AS name,
      l.last_win_at AS lastWinAt
    FROM categories c
    LEFT JOIN last_win l ON l.category_id = c.category_id
    WHERE c.user_id = :userId
      AND (l.last_win_at IS NULL OR l.last_win_at < NOW() - (:days || ' days')::interval)
    ORDER BY l.last_win_at NULLS FIRST
    """,
    nativeQuery = true)
List<CategoryNeglectedView> findNeglectedCategories(
    @Param("userId") Long userId,
    @Param("days") int days
);

}
