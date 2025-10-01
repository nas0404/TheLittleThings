// dto/friends/ChallengeResponse.java
package com.project.thelittlethings.dto.friends;

import com.project.thelittlethings.entities.FriendChallenge;
import lombok.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ChallengeResponse {
    private Long id;

    private Long challengerId;
    private String challengerUsername;

    private Long opponentId;
    private String opponentUsername;

    private String goalList;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer trophiesStake;
    private String status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    private String winnerUsername;

    public static ChallengeResponse fromEntity(FriendChallenge fc) {
        return ChallengeResponse.builder()
            .id(fc.getId())
            .challengerId(fc.getChallenger().getUserId())
            .challengerUsername(fc.getChallenger().getUsername())
            .opponentId(fc.getOpponent().getUserId())
            .opponentUsername(fc.getOpponent().getUsername())
            .goalList(fc.getGoalList())
            .startDate(fc.getStartDate())
            .endDate(fc.getEndDate())
            .trophiesStake(fc.getTrophiesStake())
            .status(fc.getStatus().name().toLowerCase())
            .createdAt(fc.getCreatedAt())
            .updatedAt(fc.getUpdatedAt())
            .winnerUsername(fc.getWinner() != null ? fc.getWinner().getUsername() : null)
            .build();
    }
}
