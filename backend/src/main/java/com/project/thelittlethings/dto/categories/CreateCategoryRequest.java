package com.project.thelittlethings.dto.categories;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    private Long userId;        // which user this category belongs to
    private String name;        // required
    private String description; // optional
}
