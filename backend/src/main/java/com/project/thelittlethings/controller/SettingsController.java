package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.settings.ProfileDto;
import com.project.thelittlethings.dto.settings.NotificationPrefsDto;
import com.project.thelittlethings.services.SettingsService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = {"http://localhost:5173", "https://thelittlethings.azurewebsites.net/"}, allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT,
        RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS
})
public class SettingsController {

    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    @GetMapping(value = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileDto getProfile(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId) {
        return service.getProfile(userId);
    }

    @PatchMapping(value = "/profile", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ProfileDto updateProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") long userId,
            @Valid @RequestBody ProfileDto dto) {
        return service.updateProfile(userId, dto);
    }

    @GetMapping(value = "/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    public NotificationPrefsDto getPrefs(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId) {
        return service.getPrefs(userId);
    }

    @PostMapping(value = "/notifications/reset", produces = MediaType.APPLICATION_JSON_VALUE)
    public NotificationPrefsDto resetPrefs(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId) {
        return service.resetPrefs(userId);
    }

    @PutMapping(value = "/notifications", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public NotificationPrefsDto updatePrefs(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") long userId,
            @RequestBody NotificationPrefsDto dto) {
        return service.updatePrefs(userId, dto);
    }
}