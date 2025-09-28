// src/test/java/com/project/thelittlethings/controller/GoalControllerTest.java
package com.project.thelittlethings.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.thelittlethings.config.RestExceptionHandler;
import com.project.thelittlethings.controller.GoalController;
import com.project.thelittlethings.dto.goals.CreateGoalRequest;
import com.project.thelittlethings.dto.goals.GoalResponse;
import com.project.thelittlethings.services.GoalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GoalController.class)
@Import(RestExceptionHandler.class)
class GoalControllerTest {

  @Autowired MockMvc mvc;
  @Autowired ObjectMapper om;

  @MockBean GoalService service;

  @Test
  void create_returns201_andLocation() throws Exception {
    var req = new CreateGoalRequest(4L, "Learn to block", "hands up", "LOW");
    var res = new GoalResponse(100L, 31L, 4L, "Learn to block", "hands up", "LOW",
        Instant.parse("2025-09-23T00:00:00Z"), Instant.parse("2025-09-23T00:00:00Z"));

    when(service.createGoal(eq(31L), eq(4L), any(CreateGoalRequest.class))).thenReturn(res);

    mvc.perform(post("/api/users/31/goals")
        .contentType(MediaType.APPLICATION_JSON)
        .content(om.writeValueAsString(req)))
      .andExpect(status().isCreated())
      .andExpect(header().string("Location", containsString("/api/users/31/goals/100")))
      .andExpect(jsonPath("$.goalId").value(100))
      .andExpect(jsonPath("$.title").value("Learn to block"));
  }

  @Test
  void list_returns200() throws Exception {
    var g1 = new GoalResponse(1L, 31L, 4L, "A", null, "HIGH", null, null);
    var g2 = new GoalResponse(2L, 31L, 4L, "B", null, "LOW", null, null);
    when(service.listGoalsByUser(31L)).thenReturn(List.of(g1, g2));

    mvc.perform(get("/api/users/31/goals"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].title").value("A"))
      .andExpect(jsonPath("$[1].title").value("B"));
  }

  @Test
  void list_withCategory_returns200() throws Exception {
    var g = new GoalResponse(3L, 31L, 4L, "Cat goal", null, "MEDIUM", null, null);
    when(service.listGoalsByUserAndCategory(31L, 4L)).thenReturn(List.of(g));

    mvc.perform(get("/api/users/31/goals?categoryId=4"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$[0].categoryId").value(4));
  }

  @Test
void grouped_priority_onlyHigh_returns200() throws Exception {
  var high = List.of(new GoalResponse(9L,31L,4L,"H",null,"HIGH",null,null));

  // if you pass no categoryId in the query string, controller should call:
  when(service.listGrouped(31L, null, "HIGH"))
      .thenReturn(Map.of("HIGH", high));

  mvc.perform(get("/api/users/31/goals/grouped?priority=HIGH"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.HIGH[0].goalId").value(9));
}
@Test
void grouped_priority_andCategory_returns200() throws Exception {
  var low = List.of(new GoalResponse(11L,31L,4L,"L",null,"LOW",null,null));

  when(service.listGrouped(31L, 4L, "LOW"))
      .thenReturn(Map.of("LOW", low));

  mvc.perform(get("/api/users/31/goals/grouped?categoryId=4&priority=LOW"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.LOW[0].goalId").value(11))
      .andExpect(jsonPath("$.LOW[0].categoryId").value(4));
}
@Test
void grouped_noPriority_returnsAllBuckets() throws Exception {
  when(service.listGrouped(31L, null, null))
      .thenReturn(Map.of(
          "HIGH", List.of(),
          "MEDIUM", List.of(),
          "LOW", List.of()
      ));

  mvc.perform(get("/api/users/31/goals/grouped"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.HIGH").isArray())
      .andExpect(jsonPath("$.MEDIUM").isArray())
      .andExpect(jsonPath("$.LOW").isArray());
}

  @Test
  void getOne_returns200() throws Exception {
    var res = new GoalResponse(10L, 31L, 4L, "One", null, "LOW", null, null);
    when(service.getOwnedGoal(10L, 31L)).thenReturn(res);

    mvc.perform(get("/api/users/31/goals/10"))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.goalId").value(10))
      .andExpect(jsonPath("$.userId").value(31));
  }

  @Test
  void getOne_notFound_returns404() throws Exception {
    when(service.getOwnedGoal(999L, 31L)).thenThrow(new IllegalArgumentException("not found"));

    mvc.perform(get("/api/users/31/goals/999"))
      .andExpect(status().isNotFound())
      .andExpect(jsonPath("$.error").value("Not Found"));
  }

  @Test
  void update_returns200() throws Exception {
    var res = new GoalResponse(10L, 31L, 4L, "NewTitle", "desc", "HIGH", null, null);
    when(service.updateGoal(eq(10L), eq(31L), any())).thenReturn(res);

    mvc.perform(put("/api/users/31/goals/10")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
          { "title":"NewTitle", "priority":"HIGH" }
        """))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.title").value("NewTitle"))
      .andExpect(jsonPath("$.priority").value("HIGH"));
  }

  @Test
  void delete_returns204() throws Exception {
    mvc.perform(delete("/api/users/31/goals/10"))
      .andExpect(status().isNoContent());
  }
}
