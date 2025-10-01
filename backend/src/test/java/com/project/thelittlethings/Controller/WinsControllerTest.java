package com.project.thelittlethings.Controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.project.thelittlethings.controller.GoalController;
import com.project.thelittlethings.controller.WinController;
import com.project.thelittlethings.services.GoalService;
import com.project.thelittlethings.services.WinService;

import jakarta.persistence.EntityNotFoundException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(GoalController.class)
class WinsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService goalService;
    @MockBean
    private WinService winService;

    @Test
    void testCompleteGoal() throws Exception {
        Long userId = 1L;
        Long goalId = 100L;

        doNothing().when(goalService).completeGoal(goalId);

        mockMvc.perform(post("/api/users/{userId}/goals/{goalId}/complete", userId, goalId))
                .andExpect(status().isOk())
                .andExpect(content().string("Goal completed and Win recorded."));
    }

}
