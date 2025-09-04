package com.project.thelittlethings.dto.challenges;

import java.time.Instant;

public class ChallengeResponse {
    private Long id;
    private String title;
    private String description;
    private int reward;
    private int progressCurrent;
    private int progressTotal;
    private ChallengeStatus status;
    private String source; // e.g. "SYSTEM_FROM_WINS"
    private Instant createdAt;
    private Instant updatedAt;

    // ---- getters & setters ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
