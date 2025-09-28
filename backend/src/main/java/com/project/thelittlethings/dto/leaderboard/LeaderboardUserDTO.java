package com.project.thelittlethings.dto.leaderboard;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaderboardUserDTO {
    private Long userId;
    private String username;
    private String region;
    private Integer trophies;
}