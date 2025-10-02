package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.settings.ProfileDto;
import com.project.thelittlethings.dto.settings.NotificationPrefsDto;
import com.project.thelittlethings.services.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = {"http://localhost:5173"})
public class SettingsController {

  private final SettingsService service;

  public SettingsController(SettingsService service) {
    this.service = service;
  }

  @GetMapping("/profile")
  public ProfileDto getProfile(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId) {
    return service.getProfile(userId);
  }

  @PatchMapping("/profile")
  public ProfileDto updateProfile(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId,
                                  @Valid @RequestBody ProfileDto dto) {
    return service.updateProfile(userId, dto);
  }

  // NEW:
  @GetMapping("/notifications")
  public NotificationPrefsDto getPrefs(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId) {
    return service.getPrefs(userId);
  }

  @PostMapping("/notifications/reset")
  public NotificationPrefsDto resetPrefs(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId) {
    return service.resetPrefs(userId);
  }
}
