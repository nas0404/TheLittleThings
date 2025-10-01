package com.project.thelittlethings.Controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import jakarta.persistence.EntityNotFoundException;

import com.project.thelittlethings.controller.GoalController;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.security.HMACtokens;
import com.project.thelittlethings.services.GoalService;
import com.project.thelittlethings.services.UserService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(GoalController.class)
class WinsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService goalService;

    @MockBean
    private UserService userService;

    @Test
    void testCompleteGoal() throws Exception {
        Long userId = 1L;
        Long goalId = 100L;
        String token = "dummy-token";
        String bearerToken = "Bearer " + token;
        String username = "testuser";

        // Create a test User object
        User testUser = new User();
        testUser.setUserId(userId);
        testUser.setUsername(username);
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setDob(LocalDate.of(1990, 1, 1));
        testUser.setAge(33);
        testUser.setGender("Male");
        testUser.setRegion("NA");
        testUser.setStreaks(5);
        testUser.setTrophies(2);
        testUser.setLastLogin(OffsetDateTime.now());

        // Mock UserService to return the testUser when finding by username
        when(userService.findByUsername(username)).thenReturn(testUser);

        // Mock static methods in HMACtokens
        try (MockedStatic<HMACtokens> mockedTokens = Mockito.mockStatic(HMACtokens.class)) {
            mockedTokens.when(() -> HMACtokens.validateToken(token)).thenReturn(true);
            mockedTokens.when(() -> HMACtokens.extractUsername(token)).thenReturn(username);

            // Mock goalService behavior
            doNothing().when(goalService).completeGoal(goalId);
            when(goalService.getOwnedGoal(goalId, userId)).thenReturn(null); // or a dummy goal response if needed

            mockMvc.perform(post("/api/goals/{goalId}/complete", userId, goalId)
                    .header("Authorization", bearerToken) // token stuff
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Goal completed and Win recorded."));
        }

    }
}