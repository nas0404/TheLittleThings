package com.project.thelittlethings.dto.categories;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    private String name;        // new name (optional)
    private String description; // new description (optional)
}