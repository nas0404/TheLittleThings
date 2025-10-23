package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification_prefs")
public class NotificationPrefs {

  @Id
  private Long userId;

  @Column(nullable=false) private boolean winDailyReminder = true;
  @Column(nullable=false) private boolean streakMilestones = true;
  @Column(nullable=false) private boolean trophies = true;
  @Column(nullable=false) private boolean weeklyChallenges = true;
  @Column(nullable=false) private boolean friendRequests = true;

  @Column(nullable=false) private boolean channelInApp = true;
  @Column(nullable=false) private boolean channelEmail = false;

  @Column(nullable=false)
  private Instant updatedAt = Instant.now();

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public boolean isWinDailyReminder() { return winDailyReminder; }
  public void setWinDailyReminder(boolean v) { this.winDailyReminder = v; }
  public boolean isStreakMilestones() { return streakMilestones; }
  public void setStreakMilestones(boolean v) { this.streakMilestones = v; }
  public boolean isTrophies() { return trophies; }
  public void setTrophies(boolean v) { this.trophies = v; }
  public boolean isWeeklyChallenges() { return weeklyChallenges; }
  public void setWeeklyChallenges(boolean v) { this.weeklyChallenges = v; }
  public boolean isFriendRequests() { return friendRequests; }
  public void setFriendRequests(boolean v) { this.friendRequests = v; }
  public boolean isChannelInApp() { return channelInApp; }
  public void setChannelInApp(boolean v) { this.channelInApp = v; }
  public boolean isChannelEmail() { return channelEmail; }
  public void setChannelEmail(boolean v) { this.channelEmail = v; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
