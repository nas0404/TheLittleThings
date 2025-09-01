package com.project.thelittlethings.dto.goals;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateGoalRequest {
    private String title;       // required if updating
    private String description; // optional
    private String priority;    // optional
    private Long categoryId;    // required if moving
}
