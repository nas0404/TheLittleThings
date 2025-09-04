package com.project.thelittlethings.entities;

import com.project.thelittlethings.dto.challenges.ChallengeStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private int reward;

    @Column(name = "progress_current", nullable = false)
    private int progressCurrent;

    @Column(name = "progress_total", nullable = false)
    private int progressTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;

    private String source; // e.g. "SYSTEM_FROM_WINS"

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant updatedAt;

    // ---------- getters & setters ----------

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getReward() { return reward; }
    public void setReward(int reward) { this.reward = reward; }

    public int getProgressCurrent() { return progressCurrent; }
    public void setProgressCurrent(int progressCurrent) { this.progressCurrent = progressCurrent; }

    public int getProgressTotal() { return progressTotal; }
    public void setProgressTotal(int progressTotal) { this.progressTotal = progressTotal; }

    public ChallengeStatus getStatus() { return status; }
    public void setStatus(ChallengeStatus status) { this.status = status; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
