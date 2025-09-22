package com.project.thelittlethings.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.thelittlethings.config.RestExceptionHandler;
import com.project.thelittlethings.controller.CategoryController;
import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.dto.categories.CreateCategoryRequest;
import com.project.thelittlethings.services.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CategoryController.class) 
@Import(RestExceptionHandler.class)                  
class CategoryControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean CategoryService service;

    // @MockBean JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Test
    void create_returns201_andBody() throws Exception {
        var req = new CreateCategoryRequest(null, "Fitness", "Gym");
        var res = new CategoryResponse(1L, 31L, "Fitness", "Gym", null, null);

        when(service.create(eq(31L), any(CreateCategoryRequest.class))).thenReturn(res);

        mvc.perform(post("/api/users/31/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location",
                    org.hamcrest.Matchers.containsString("/api/users/31/categories/1")))
            .andExpect(jsonPath("$.categoryId").value(1))
            .andExpect(jsonPath("$.name").value("Fitness"));
    }

    @Test
    void get_returns200() throws Exception {
        var res = new CategoryResponse(5L, 31L, "Boxing", "Move", null, null);
        when(service.getOwned(5L, 31L)).thenReturn(res);

        mvc.perform(get("/api/users/31/categories/5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.categoryId").value(5))
            .andExpect(jsonPath("$.userId").value(31));
    }
}
