package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.settings.ProfileDto;
import com.project.thelittlethings.dto.settings.NotificationPrefsDto;

public interface SettingsService {
  ProfileDto getProfile(long userId);
  ProfileDto updateProfile(long userId, ProfileDto dto);

  NotificationPrefsDto getPrefs(long userId);
  NotificationPrefsDto resetPrefs(long userId);
}
