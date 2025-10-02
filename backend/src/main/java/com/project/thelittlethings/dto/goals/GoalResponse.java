package com.project.thelittlethings.dto.goals;

import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;

// DTO class for sending goal data in API responses
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
    private Instant createdAt;
    private Instant updatedAt;
}
