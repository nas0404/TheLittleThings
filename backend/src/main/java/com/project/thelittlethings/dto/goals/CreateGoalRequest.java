package com.project.thelittlethings.dto.goals;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGoalRequest {
    private Long userId;       // required
    private Long categoryId;   // required
    private String title;      // required
    private String description; // optional
    private String priority;    // required ("HIGH", "MEDIUM", "LOW")
}