package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.settings.ProfileDto;
import com.project.thelittlethings.dto.settings.NotificationPrefsDto;
import com.project.thelittlethings.entities.UserProfile;
import com.project.thelittlethings.entities.NotificationPrefs;
import com.project.thelittlethings.repositories.UserProfileRepository;
import com.project.thelittlethings.repositories.NotificationPrefsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SettingsServiceImpl implements SettingsService {

  private final UserProfileRepository profiles;
  private final NotificationPrefsRepository prefsRepo;

  public SettingsServiceImpl(UserProfileRepository profiles,
      NotificationPrefsRepository prefsRepo) {
    this.profiles = profiles;
    this.prefsRepo = prefsRepo;
  }

  // ---- Profile (existing) ----
  private static ProfileDto toDto(UserProfile p) {
    return new ProfileDto(p.getDisplayName(), p.getBio(), p.getAvatarUrl());
  }

  @Override
  @Transactional(readOnly = true)
  public ProfileDto getProfile(long userId) {
    UserProfile p = profiles.findById(userId).orElseGet(() -> {
      UserProfile np = new UserProfile();
      np.setUserId(userId);
      np.setDisplayName("New User");
      np.setUpdatedAt(Instant.now());
      return np;
    });
    return toDto(p);
  }

  @Override
  @Transactional
  public ProfileDto updateProfile(long userId, ProfileDto dto) {
    UserProfile p = profiles.findById(userId).orElseGet(() -> {
      UserProfile np = new UserProfile();
      np.setUserId(userId);
      return np;
    });
    p.setDisplayName(dto.displayName());
    p.setBio(dto.bio());
    p.setAvatarUrl(dto.avatarUrl());
    p.setUpdatedAt(Instant.now());
    return toDto(profiles.save(p));
  }

  // ---- Notifications (existing helpers) ----
  private static NotificationPrefsDto toDto(NotificationPrefs p) {
    return new NotificationPrefsDto(
        p.isWinDailyReminder(),
        p.isStreakMilestones(),
        p.isTrophies(),
        p.isWeeklyChallenges(),
        p.isFriendRequests(),
        p.isChannelInApp(),
        p.isChannelEmail());
  }

  @Override
  @Transactional(readOnly = true)
  public NotificationPrefsDto getPrefs(long userId) {
    NotificationPrefs p = prefsRepo.findById(userId).orElseGet(() -> {
      NotificationPrefs d = new NotificationPrefs(); // defaults
      d.setUserId(userId);
      return d; // not saved on GET
    });
    return toDto(p);
  }

  @Override
  @Transactional
  public NotificationPrefsDto resetPrefs(long userId) {
    NotificationPrefs p = prefsRepo.findById(userId).orElseGet(() -> {
      NotificationPrefs d = new NotificationPrefs();
      d.setUserId(userId);
      return d;
    });
    p.setWinDailyReminder(true);
    p.setStreakMilestones(true);
    p.setTrophies(true);
    p.setWeeklyChallenges(true);
    p.setFriendRequests(true);
    p.setChannelInApp(true);
    p.setChannelEmail(false);
    p.setUpdatedAt(Instant.now());
    return toDto(prefsRepo.save(p));
  }

  // ---- NEW: Update prefs (covers #77 and sets you up for #76) ----
  @Override
  @Transactional
  public NotificationPrefsDto updatePrefs(long userId, NotificationPrefsDto dto) {
    NotificationPrefs p = prefsRepo.findById(userId).orElseGet(() -> {
      NotificationPrefs d = new NotificationPrefs();
      d.setUserId(userId);
      return d;
    });
    p.setWinDailyReminder(dto.winDailyReminder());
    p.setStreakMilestones(dto.streakMilestones());
    p.setTrophies(dto.trophies());
    p.setWeeklyChallenges(dto.weeklyChallenges());
    p.setFriendRequests(dto.friendRequests());
    p.setChannelInApp(dto.channelInApp());
    p.setChannelEmail(dto.channelEmail());
    p.setUpdatedAt(Instant.now());
    return toDto(prefsRepo.save(p));
  }
}
