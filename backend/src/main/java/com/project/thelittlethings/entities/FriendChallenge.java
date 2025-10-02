package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "friend_challenges")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FriendChallenge {

    public enum Status { PROPOSED, ACCEPTED, DECLINED, ACTIVE, COMPLETION_REQUESTED, COMPLETED, EXPIRED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "challenger_id", nullable = false)
    private User challenger;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "opponent_id", nullable = false)
    private User opponent;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goalList;

    private LocalDate startDate;
    private LocalDate endDate;

    @Builder.Default
    private Integer trophiesStake = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Builder.Default
    private Status status = Status.PROPOSED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_user_id")
    private User winner;

    @Builder.Default
    @Column(name = "escrowed", nullable = false)
    private boolean escrowed = false;   // true once the stake is deducted from both sides
    
    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completion_requested_by")
    private User completionRequestedBy;

    @Column(name = "completion_requested_at")
    private java.time.OffsetDateTime completionRequestedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "completion_confirmed_by")
    private User completionConfirmedBy;

    @Column(name = "completion_confirmed_at")
    private java.time.OffsetDateTime completionConfirmedAt;

}
