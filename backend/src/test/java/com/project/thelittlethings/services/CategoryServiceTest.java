package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.categories.CreateCategoryRequest;
import com.project.thelittlethings.dto.categories.UpdateCategoryRequest;
import com.project.thelittlethings.dto.categories.CategoryResponse;
import com.project.thelittlethings.entities.Category;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.CategoryRepository;
import com.project.thelittlethings.repositories.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    private CategoryRepository categoryRepo;
    private UserRepository userRepo;
    private CategoryService service;

    @BeforeEach
    void setup() {
        categoryRepo = mock(CategoryRepository.class);
        userRepo = mock(UserRepository.class);
        MockitoAnnotations.openMocks(this);
        service = new CategoryService(categoryRepo, userRepo);
    }

    private User mockUser(long id) {
        User u = new User();
        u.setUserId(id);
        return u;
    }

    private Category mockCategory(User u, long id, String name, String desc) {
        Category c = new Category();
        c.setCategoryId(id);
        c.setUser(u);
        c.setName(name);
        c.setDescription(desc);
        c.setCreatedAt(Instant.now());
        c.setUpdatedAt(Instant.now());
        return c;
    }

    @Test
    void create_success() {
        User user = mockUser(10L);
        when(userRepo.findById(10L)).thenReturn(Optional.of(user));
        when(categoryRepo.existsByUser_UserIdAndName(10L, "Fitness")).thenReturn(false);

        when(categoryRepo.save(any(Category.class))).thenAnswer(inv -> {
            Category c = inv.getArgument(0);
            c.setCategoryId(1L);
            return c;
        });

        var req = new CreateCategoryRequest("Fitness", "Gym");
        CategoryResponse res = service.create(10L, req);

        assertEquals(1L, res.getCategoryId());
        assertEquals("Fitness", res.getName());
        verify(categoryRepo).save(any(Category.class));
    }


    @Test
    void create_fail_missingName() {
        User user = mockUser(10L);
        when(userRepo.findById(10L)).thenReturn(Optional.of(user));

        var req = new CreateCategoryRequest("   ", null);
        var ex = assertThrows(IllegalArgumentException.class,
                () -> service.create(10L, req));
        assertTrue(ex.getMessage().toLowerCase().contains("name is required"));
    }




    @Test
    void update_success() {
        User user = mockUser(10L);
        Category cat = mockCategory(user, 1L, "Old", "desc");

        when(categoryRepo.findByCategoryIdAndUser_UserId(1L, 10L))
            .thenReturn(Optional.of(cat));

        when(categoryRepo.existsByUser_UserIdAndName(10L, "New")).thenReturn(false);

        when(categoryRepo.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new UpdateCategoryRequest("New", "Updated");
        CategoryResponse res = service.update(1L, 10L, req);

        assertEquals("New", res.getName());
        assertEquals("Updated", res.getDescription());
    }


    @Test
    void delete_success() {
      User user = mockUser(10L);
      Category cat = mockCategory(user, 5L, "Temp", null);

      when(categoryRepo.findByCategoryIdAndUser_UserId(5L, 10L))
          .thenReturn(Optional.of(cat));

      service.delete(5L, 10L);

      verify(categoryRepo).delete(cat);
    }

    @Test
    void listByUser_success() {
        User user = mockUser(10L);
        Category cat1 = mockCategory(user, 1L, "A", null);
        Category cat2 = mockCategory(user, 2L, "B", null);

        when(userRepo.existsById(10L)).thenReturn(true);

        when(categoryRepo.findByUser_UserId(10L)).thenReturn(List.of(cat1, cat2));

        List<CategoryResponse> list = service.listByUser(10L);
        assertEquals(2, list.size());
        assertEquals("A", list.get(0).getName());
    }

}
