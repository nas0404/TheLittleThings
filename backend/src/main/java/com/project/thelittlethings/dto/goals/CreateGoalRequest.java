package com.project.thelittlethings.dto.goals;

import jakarta.validation.constraints.*;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class CreateGoalRequest {

  @NotNull(message = "categoryId is required")
  @Positive(message = "categoryId must be a positive number")
  private Long categoryId;

  @NotBlank(message = "title is required")
  @Size(max = 255, message = "title must be ≤ 255 characters")
  private String title;

  @Size(max = 1000, message = "description must be ≤ 1000 characters")
  private String description;

  @NotBlank(message = "priority is required")
  @Pattern(regexp = "^(?i)(HIGH|MEDIUM|LOW)$",
           message = "priority must be HIGH, MEDIUM, or LOW")
  private String priority;
}

