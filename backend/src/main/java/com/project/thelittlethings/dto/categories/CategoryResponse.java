package com.project.thelittlethings.dto.categories;

import com.project.thelittlethings.entities.Category;
import lombok.*;
import java.time.Instant;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CategoryResponse {
  private Long categoryId;
  private Long userId;
  private String name;
  private String description;
  private Instant createdAt;
  private Instant updatedAt;

  public static CategoryResponse from(Category c) {
    return new CategoryResponse(
      c.getCategoryId(),
      c.getUser().getUserId(),
      c.getName(),
      c.getDescription(),
      c.getCreatedAt(),
      c.getUpdatedAt()
    );
  }
}
