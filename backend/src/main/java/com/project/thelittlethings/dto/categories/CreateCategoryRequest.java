package com.project.thelittlethings.dto.categories;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateCategoryRequest {

  @NotBlank(message = "name is required")
  @Size(max = 100, message = "name must be ≤ 100 characters")
  private String name;

  @Size(max = 100, message = "description must be ≤ 100 characters")
  private String description;
}
