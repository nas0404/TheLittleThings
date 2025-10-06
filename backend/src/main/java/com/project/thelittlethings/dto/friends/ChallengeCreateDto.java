package com.project.thelittlethings.dto.friends;
import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChallengeCreateDto {
    private Long opponentId;
    private String goalList;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer trophiesStake;
}
