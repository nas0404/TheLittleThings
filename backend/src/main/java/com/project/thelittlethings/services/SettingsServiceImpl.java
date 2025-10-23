package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.settings.NotificationPrefsDto;
import com.project.thelittlethings.dto.settings.ProfileDto;
import com.project.thelittlethings.entities.NotificationPrefs;
import com.project.thelittlethings.entities.UserProfile;
import com.project.thelittlethings.repositories.NotificationPrefsRepository;
import com.project.thelittlethings.repositories.UserProfileRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Service
public class SettingsServiceImpl implements SettingsService {

  private final NotificationPrefsRepository prefsRepo;
  private final UserProfileRepository profiles; // << add back

  public SettingsServiceImpl(NotificationPrefsRepository prefsRepo,
                             UserProfileRepository profiles) {
    this.prefsRepo = prefsRepo;
    this.profiles = profiles; // << add back
  }

  // -------------- Notifications (unchanged) --------------
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
      NotificationPrefs d = new NotificationPrefs();
      d.setUserId(userId);
      return d;
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

  // -------------- Profiles (implement these) --------------

  private static ProfileDto toProfileDto(UserProfile p) {
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
      // do not save on GET; only materialize on first update
      return np;
    });
    return toProfileDto(p);
  }

  @Override
  @Transactional
  public ProfileDto updateProfile(long userId, ProfileDto dto) {
    UserProfile p = profiles.findById(userId).orElseGet(() -> {
      UserProfile np = new UserProfile();
      np.setUserId(userId);
      return np;
    });
    // null-safe updates
    if (dto.displayName() != null) p.setDisplayName(dto.displayName().trim());
    if (dto.bio() != null)        p.setBio(dto.bio().trim());
    if (dto.avatarUrl() != null)  p.setAvatarUrl(dto.avatarUrl().trim());
    p.setUpdatedAt(Instant.now());

    return toProfileDto(profiles.save(p));
  }
}
