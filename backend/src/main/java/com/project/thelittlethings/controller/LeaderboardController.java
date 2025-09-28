package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.leaderboard.LeaderboardUserDTO;
import com.project.thelittlethings.services.LeaderboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    public List<LeaderboardUserDTO> getLeaderboard(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return leaderboardService.getLeaderboard(region, page, size);
    }
}