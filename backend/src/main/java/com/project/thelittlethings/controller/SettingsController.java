package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.settings.ProfileDto;
import com.project.thelittlethings.services.SettingsService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings") // your chosen base path
@CrossOrigin(origins = "http://localhost:3000") // allow your frontend to call it
public class SettingsController {

    private final SettingsService service;

    public SettingsController(SettingsService service) {
        this.service = service;
    }

    // Demo: use header for user id; default to 1
    @GetMapping("/profile")
    public ProfileDto getProfile(@RequestHeader(value = "X-User-Id", defaultValue = "1") long userId) {
        return service.getProfile(userId);
    }

    @PatchMapping("/profile")
    public ProfileDto updateProfile(
            @RequestHeader(value = "X-User-Id", defaultValue = "1") long userId,
            @Valid @RequestBody ProfileDto dto) {
        return service.updateProfile(userId, dto);
    }
}
