package com.project.thelittlethings.dto.settings;

public record NotificationPrefsDto(
  boolean winDailyReminder,
  boolean streakMilestones,
  boolean trophies,
  boolean weeklyChallenges,
  boolean friendRequests,
  boolean channelInApp,
  boolean channelEmail
) {}
