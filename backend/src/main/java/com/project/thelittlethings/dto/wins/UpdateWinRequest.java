package com.project.thelittlethings.dto.wins;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class UpdateWinRequest {
    private String title;
    private String description;
    private Integer numTrophies;
    private Long goalId;
    private Long journalId;
}
