package com.project.thelittlethings.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.project.thelittlethings.MaterialisedView.CategoryNeglectedView;
import com.project.thelittlethings.View.CategoryNeglectView;
import com.project.thelittlethings.entities.Category;
import com.project.thelittlethings.entities.User;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByUser_UserId(Long userId);
    boolean existsByUser_UserIdAndName(Long userId, String name);
    boolean existsByUser_UserId(Long userId);


    // Returns all categories for a user that are considered "neglected".
    // A category is neglected if:
    //   1) It has no wins within the last :days interval, AND
    //   2) Its last activity (latest win OR creation date if no wins exist)
    //      is older than :days.
    // For each neglected category, we return:
    //   - Basic info (id, userId, name, description)
    //   - lastWinAt  → timestamp of last activity
    //   - neglectDays → days since last activity, rounded up
    // Results are ordered from the most neglected (oldest activity) to least.
    @Query(value = """
      SELECT
        c.category_id AS categoryId,
        c.user_id     AS userId,
        c.name        AS name,
        c.description AS description,
        COALESCE(MAX(w.completion_date), c.created_at) AS lastWinAt,
        CEIL(EXTRACT(EPOCH FROM (NOW() - COALESCE(MAX(w.completion_date), c.created_at))) / 86400.0)::bigint AS neglectDays
      FROM categories c
      LEFT JOIN goals g ON g.category_id = c.category_id AND g.user_id = c.user_id
      LEFT JOIN wins  w ON w.goal_id     = g.goal_id     AND w.user_id = c.user_id AND w.completion_date IS NOT NULL
      WHERE c.user_id = :userId
      GROUP BY c.category_id, c.user_id, c.name, c.description, c.created_at
      HAVING
        -- 1) no wins in the lookback window
        SUM(
          CASE WHEN w.completion_date IS NOT NULL
                AND w.completion_date >= NOW() - CAST(:days || ' days' AS interval)
              THEN 1 ELSE 0
          END
        ) = 0
        -- 2) and the last activity (win or created_at) is older than the window
        AND COALESCE(MAX(w.completion_date), c.created_at) < NOW() - CAST(:days || ' days' AS interval)
      ORDER BY COALESCE(MAX(w.completion_date), c.created_at) ASC
      """, nativeQuery = true)
    List<CategoryNeglectedView> findNeglectedCategories(@Param("userId") Long userId,
                                                        @Param("days") int days);
    Optional<Category> findByCategoryIdAndUser_UserId(Long categoryId, Long userId);
    }