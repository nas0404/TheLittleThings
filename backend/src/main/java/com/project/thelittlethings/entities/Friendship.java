package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(
    name = "friendships",
    uniqueConstraints = @UniqueConstraint(name = "uq_friend_pair", columnNames = {"userA_id","userB_id"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Friendship {

    public enum Status { PENDING, ACCEPTED, DECLINED, CANCELED, BLOCKED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Canonical ordered pair: userA.id < userB.id (enforced in service)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userA_id", nullable = false)
    private User userA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userB_id", nullable = false)
    private User userB;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private Status status = Status.PENDING;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responded_by")
    private User respondedBy;

    @Column(name = "requested_at", nullable = false)
    @Builder.Default
    private OffsetDateTime requestedAt = OffsetDateTime.now();

    @Column(name = "responded_at")
    private OffsetDateTime respondedAt;

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PrePersist
    public void prePersist() {
        if (requestedAt == null) requestedAt = OffsetDateTime.now();
        if (updatedAt == null)   updatedAt   = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
