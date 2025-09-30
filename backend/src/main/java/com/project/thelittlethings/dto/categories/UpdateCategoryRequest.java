package com.project.thelittlethings.dto.categories;

import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateCategoryRequest {
  // optional; if present, enforce length
  @Size(max = 100, message = "name must be ≤ 100 characters")
  private String name;

  @Size(max = 1000, message = "description must be ≤ 1000 characters")
  private String description;
}