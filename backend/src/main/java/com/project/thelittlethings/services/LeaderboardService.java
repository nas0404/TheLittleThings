package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.leaderboard.LeaderboardUserDTO;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final UserRepository userRepository;

    public List<LeaderboardUserDTO> getLeaderboard(String region, int page, int size) {
        List<User> users;

        if (region != null && !region.isEmpty()) {
            users = userRepository.findByRegionOrderByTrophiesDesc(region, PageRequest.of(page, size));
        } else {
            users = userRepository.findAllByOrderByTrophiesDesc(PageRequest.of(page, size));
        }

        return users.stream()
                .map(u -> new LeaderboardUserDTO(u.getUserId(), u.getUsername(), u.getRegion(), u.getTrophies()))
                .collect(Collectors.toList());
    }
}