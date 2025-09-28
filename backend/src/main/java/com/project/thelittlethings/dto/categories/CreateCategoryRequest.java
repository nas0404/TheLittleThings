package com.project.thelittlethings.dto.categories;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    private String name;        // required
    private String description; // optional
}
