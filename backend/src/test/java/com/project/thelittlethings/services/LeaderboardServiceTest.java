package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.leaderboard.LeaderboardUserDTO;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LeaderboardService leaderboardService;

    @Test
    void testGetLeaderboard_withRegion() {
        String region = "Europe";
        int page = 0;
        int size = 5;

        User user1 = new User();
        user1.setUserId(1L);
        user1.setUsername("user1");
        user1.setRegion("Europe");
        user1.setTrophies(100);

        User user2 = new User();
        user2.setUserId(2L);
        user2.setUsername("user2");
        user2.setRegion("Europe");
        user2.setTrophies(90);

        List<User> mockUsers = List.of(user1, user2);

        when(userRepository.findByRegionOrderByTrophiesDesc(eq(region), any(PageRequest.class)))
                .thenReturn(mockUsers);

        List<LeaderboardUserDTO> result = leaderboardService.getLeaderboard(region, page, size);

        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals(100, result.get(0).getTrophies());

        verify(userRepository).findByRegionOrderByTrophiesDesc(eq(region), any(PageRequest.class));
        verify(userRepository, never()).findAllByOrderByTrophiesDesc(any(PageRequest.class));
    }

    @Test
    void testGetLeaderboard_withoutRegion() {
        int page = 0;
        int size = 5;

        User user3 = new User();
        user3.setUserId(3L);
        user3.setUsername("user3");
        user3.setRegion("Asia");
        user3.setTrophies(80);

        User user4 = new User();
        user4.setUserId(4L);
        user4.setUsername("user4");
        user4.setRegion("America");
        user4.setTrophies(70);

        List<User> mockUsers = List.of(user3, user4);

        when(userRepository.findAllByOrderByTrophiesDesc(any(PageRequest.class)))
                .thenReturn(mockUsers);

        List<LeaderboardUserDTO> result = leaderboardService.getLeaderboard(null, page, size);

        assertEquals(2, result.size());
        assertEquals("user3", result.get(0).getUsername());

        verify(userRepository).findAllByOrderByTrophiesDesc(any(PageRequest.class));
        verify(userRepository, never()).findByRegionOrderByTrophiesDesc(anyString(), any(PageRequest.class));
    }
}