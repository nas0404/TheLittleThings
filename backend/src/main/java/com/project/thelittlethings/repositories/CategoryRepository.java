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

    @Query(
  value =
  "SELECT " +
  "  c.category_id AS categoryId, " +
  "  c.user_id     AS userId, " +
  "  c.name        AS name, " +
  "  c.description AS description, " +
  "  COALESCE(MAX(w.completion_date), c.created_at) AS lastWinAt, " +
  "  SUM(CASE WHEN w.completion_date IS NOT NULL " +
  "            AND w.completion_date >= NOW() - CAST(:days || ' days' AS interval) " +
  "      THEN 1 ELSE 0 END) AS recentWins, " +
  "  CEIL(EXTRACT(EPOCH FROM (NOW() - COALESCE(MAX(w.completion_date), c.created_at))) / 86400.0)::bigint AS neglectDays " +
  "FROM categories c " +
  "LEFT JOIN goals g ON g.category_id = c.category_id AND g.user_id = c.user_id " +
  "LEFT JOIN wins  w ON w.goal_id     = g.goal_id     AND w.user_id = c.user_id AND w.completion_date IS NOT NULL " +
  "WHERE c.user_id = :userId " +
  "GROUP BY c.category_id, c.user_id, c.name, c.description, c.created_at " +
  "ORDER BY (MAX(w.completion_date) IS NULL) DESC, COALESCE(MAX(w.completion_date), c.created_at) ASC",
  nativeQuery = true
)
List<CategoryNeglectView> findNeglectedByUser(@Param("userId") Long userId,
                                              @Param("days") int days);
}

