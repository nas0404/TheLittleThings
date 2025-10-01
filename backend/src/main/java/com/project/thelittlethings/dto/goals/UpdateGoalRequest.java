package com.project.thelittlethings.dto.goals;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data @NoArgsConstructor @AllArgsConstructor
public class UpdateGoalRequest {

  @Size(max = 255, message = "title must be ≤ 255 characters")
  private String title;          

  @Size(max = 100, message = "description must be ≤ 100 characters")
  private String description;     

  @Pattern(regexp = "^(?i)(HIGH|MEDIUM|LOW)$",
           message = "priority must be HIGH, MEDIUM, or LOW")
  private String priority;        

  @Positive(message = "category must be valid")
  private Long categoryId;        
}

