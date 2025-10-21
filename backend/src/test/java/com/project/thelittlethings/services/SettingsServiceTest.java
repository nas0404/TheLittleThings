package com.project.thelittlethings.services;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.project.thelittlethings.repositories.UserProfileRepository;
import com.project.thelittlethings.entities.UserProfile;
import com.project.thelittlethings.dto.settings.*;
import com.project.thelittlethings.controller.SettingsController;
import com.project.thelittlethings.services.SettingsService;
import com.project.thelittlethings.services.SettingsServiceImpl;
/*
class SettingsServiceTest {

    @Mock
    UserProfileRepository settingsRepo;

    SettingsService settingsService;

    UserProfile testSettings;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        settingsService = new SettingsService(settingsRepo);

        testSettings = new UserSettings();
        testSettings.setId(1L);
        testSettings.setUserId(10L);
        testSettings.setEmailNotifications(true);
        testSettings.setPushNotifications(false);
        testSettings.setTheme("dark");
        testSettings.setLanguage("en");
    }

    @Test
    void testGetSettings() {
        when(settingsRepo.findByUserId(10L)).thenReturn(Optional.of(testSettings));

        UserProfile result = settingsService.getSettings(10L);

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        assertEquals("dark", result.getTheme());
        verify(settingsRepo).findByUserId(10L);
    }

    @Test
    void testGetSettingsWhenNotFoundCreatesDefault() {
        when(settingsRepo.findByUserId(99L)).thenReturn(Optional.empty());

        UserProfile result = settingsService.getSettings(99L);

        assertNotNull(result);
        assertEquals(99L, result.getUserId());
        verify(settingsRepo).save(any(UserSettings.class));
    }

    @Test
    void testUpdateSettings() {
        when(settingsRepo.findByUserId(10L)).thenReturn(Optional.of(testSettings));
        when(settingsRepo.save(any(UserSettings.class))).thenReturn(testSettings);

        var req = new UpdateSettingsRequest();
        req.setEmailNotifications(false);
        req.setPushNotifications(true);
        req.setTheme("light");
        req.setLanguage("fr");

        UserSettings updated = settingsService.updateSettings(10L, req);

        assertNotNull(updated);
        assertEquals("light", updated.getTheme());
        assertTrue(updated.getPushNotifications());
        verify(settingsRepo).save(any(UserSettings.class));
    }

    @Test
    void testUpdateSettingsCreatesNewIfMissing() {
        when(settingsRepo.findByUserId(50L)).thenReturn(Optional.empty());
        when(settingsRepo.save(any(UserSettings.class))).thenReturn(testSettings);

        var req = new UpdateSettingsRequest();
        req.setEmailNotifications(true);
        req.setPushNotifications(false);
        req.setTheme("dark");
        req.setLanguage("en");

        UserSettings created = settingsService.updateSettings(50L, req);

        assertNotNull(created);
        assertEquals(50L, created.getUserId());
        verify(settingsRepo).save(any(UserSettings.class));
    }

    @Test
    void testInvalidThemeThrowsException() {
        var req = new UpdateSettingsRequest();
        req.setTheme("neon-purple"); // invalid theme
        req.setLanguage("en");

        assertThrows(IllegalArgumentException.class, () -> settingsService.validateTheme(req.getTheme()));
    }
} */