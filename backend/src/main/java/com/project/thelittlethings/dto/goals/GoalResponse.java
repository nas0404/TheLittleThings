package com.project.thelittlethings.dto.goals;

import lombok.*;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private Long goalId;
    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private String priority;
    private OffsetDateTime createdAt;
}
