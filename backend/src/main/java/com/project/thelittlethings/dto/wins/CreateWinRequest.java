package com.project.thelittlethings.dto.wins;

import java.time.OffsetDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateWinRequest {
    private Long userId;
    private Long goalId;
    private String title;
    private Integer numTrophies;
    private OffsetDateTime completionDate;
    private String description;
    private Long journalId;
}
