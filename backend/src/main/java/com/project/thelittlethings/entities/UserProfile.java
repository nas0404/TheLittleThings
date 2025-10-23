package com.project.thelittlethings.entities;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
  @Id
  private Long userId; // same id as your users table (simple demo)

  @Column(nullable=false, length=100)
  private String displayName;

  @Column(columnDefinition="text")
  private String bio;

  @Column(columnDefinition="text")
  private String avatarUrl;

  @Column(nullable=false)
  private Instant updatedAt = Instant.now();

  public Long getUserId() { return userId; }
  public void setUserId(Long userId) { this.userId = userId; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public String getBio() { return bio; }
  public void setBio(String bio) { this.bio = bio; }
  public String getAvatarUrl() { return avatarUrl; }
  public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
  public Instant getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}