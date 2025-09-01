package com.project.thelittlethings.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.thelittlethings.entities.Category;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find all categories that belong to a specific user (by userId).
    List<Category> findByUserId(Long userId);
    boolean existsByUser_UserIdAndName(Long userId, String name);


    // Check if this user has ANY categories
    boolean existsByUserId(Long userId, String name);

}
