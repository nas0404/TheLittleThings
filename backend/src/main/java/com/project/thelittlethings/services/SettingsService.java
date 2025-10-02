package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.settings.ProfileDto;

public interface SettingsService {
  ProfileDto getProfile(long userId);
  ProfileDto updateProfile(long userId, ProfileDto dto);
}
