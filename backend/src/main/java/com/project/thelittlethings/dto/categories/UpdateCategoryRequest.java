package com.project.thelittlethings.dto.categories;

import lombok.*;
import jakarta.validation.constraints.Size;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateCategoryRequest {

  @Size(max = 100, message = "name must be ≤ 100 characters")
  private String name;

  @Size(max = 100, message = "description must be ≤ 100 characters")
  private String description;
}
