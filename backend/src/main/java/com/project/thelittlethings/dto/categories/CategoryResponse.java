package com.project.thelittlethings.dto.categories;

import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long categoryId;
    private Long userId;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}