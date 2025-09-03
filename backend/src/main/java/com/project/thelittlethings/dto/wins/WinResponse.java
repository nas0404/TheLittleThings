package com.project.thelittlethings.dto.wins;

import java.time.OffsetDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class WinResponse {
    private Long winId;
    private Long userId;
    private Long goalId;
    private String title;
    private String description;
    private Integer numTrophies;
    private OffsetDateTime completionDate;
    private Long journalId;
}
