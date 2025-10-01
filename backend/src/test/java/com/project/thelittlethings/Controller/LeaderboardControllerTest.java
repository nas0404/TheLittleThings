package com.project.thelittlethings.Controller;

import com.project.thelittlethings.controller.LeaderboardController;
import com.project.thelittlethings.dto.leaderboard.LeaderboardUserDTO;
import com.project.thelittlethings.services.LeaderboardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LeaderboardController.class)
class LeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LeaderboardService leaderboardService;

    @Test
    void testGetLeaderboard_withRegion() throws Exception {
        List<LeaderboardUserDTO> mockResponse = List.of(
                new LeaderboardUserDTO(1L, "user1", "Europe", 100));

        Mockito.when(leaderboardService.getLeaderboard("Europe", 0, 10))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/leaderboard")
                .param("region", "Europe")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user1"))
                .andExpect(jsonPath("$[0].region").value("Europe"))
                .andExpect(jsonPath("$[0].trophies").value(100));
    }

    @Test
    void testGetLeaderboard_withoutRegion() throws Exception {
        List<LeaderboardUserDTO> mockResponse = List.of(
                new LeaderboardUserDTO(2L, "user2", "Asia", 80));

        Mockito.when(leaderboardService.getLeaderboard(null, 0, 10))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/leaderboard")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("user2"))
                .andExpect(jsonPath("$[0].region").value("Asia"))
                .andExpect(jsonPath("$[0].trophies").value(80));
    }
}
