package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.wins.CreateWinRequest;
import com.project.thelittlethings.dto.wins.UpdateWinRequest;
import com.project.thelittlethings.dto.wins.WinResponse;
import com.project.thelittlethings.entities.Goal;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.entities.Win;
import com.project.thelittlethings.repositories.GoalRepository;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.repositories.WinRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WinService {

    private final WinRepository winRepo;
    private final UserRepository userRepo;
    private final GoalRepository goalRepo;

    public WinService(WinRepository winRepo, UserRepository userRepo, GoalRepository goalRepo) {
        this.winRepo = winRepo;
        this.userRepo = userRepo;
        this.goalRepo = goalRepo;
    }

    private WinResponse toResponse(Win win) {
        var r = new WinResponse();
        r.setWinId(win.getWinId());
        r.setUserId(win.getUser().getUserId());
        r.setGoalId(win.getGoal().getGoalId());
        r.setTitle(win.getTitle());
        r.setDescription(win.getDescription());
        r.setCompletionDate(win.getCompletionDate());
        r.setNumTrophies(win.getNumTrophies());
        if (win.getJournalId() != null) r.setJournalId(win.getJournalId());
        return r;
    }

    @Transactional
    public WinResponse createWin(CreateWinRequest req) {
        // Validate required fields
        if (req.getUserId() == null) throw new IllegalArgumentException("userId is required");
        if (req.getGoalId() == null) throw new IllegalArgumentException("goalId is required");
        if (req.getTitle() == null || req.getTitle().trim().isEmpty())
            throw new IllegalArgumentException("title is required");
        if (req.getCompletionDate() == null)
            throw new IllegalArgumentException("completionDate is required");
        if (req.getNumTrophies() == null)
            throw new IllegalArgumentException("numTrophies is required");

        // Fetch related entities
        User user = userRepo.findById(req.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("user not found"));

        Goal goal = goalRepo.findById(req.getGoalId())
                .orElseThrow(() -> new IllegalArgumentException("goal not found"));

        // Create win
        Win win = new Win();
        win.setUser(user);
        win.setGoal(goal);
        win.setTitle(req.getTitle().trim());
        win.setNumTrophies(Math.max(0, req.getNumTrophies())); // clamp to non-negative
        win.setCompletionDate(req.getCompletionDate());
        win.setDescription(req.getDescription());
        win.setJournalId(req.getJournalId());

        Win saved = winRepo.save(win);

        // Add trophies to user
        int toAdd = saved.getNumTrophies() == null ? 0 : saved.getNumTrophies();
        int current = user.getTrophies() == null ? 0 : user.getTrophies();
        user.setTrophies(current + toAdd);
        userRepo.save(user);

        return toResponse(saved);
    }

    @Transactional
    public WinResponse updateWin(Long winId, Long userId, UpdateWinRequest req) {
        Win win = winRepo.findByWinIdAndUser_UserId(winId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Win not found or not owned by user"));

        if (req.getTitle() != null && !req.getTitle().trim().isEmpty()) {
            win.setTitle(req.getTitle().trim());
        }
        if (req.getDescription() != null) {
            win.setDescription(req.getDescription());
        }

        if (req.getNumTrophies() != null) {
            int oldVal = win.getNumTrophies() == null ? 0 : win.getNumTrophies();
            int newVal = Math.max(0, req.getNumTrophies());
            int delta = newVal - oldVal;

            if (delta != 0) {
                User user = win.getUser(); // already associated
                int current = user.getTrophies() == null ? 0 : user.getTrophies();
                int updated = current + delta;
                user.setTrophies(Math.max(0, updated)); // keep non-negative total
                userRepo.save(user);
            }
            win.setNumTrophies(newVal);
        }

        Win updatedWin = winRepo.save(win);
        return toResponse(updatedWin);
    }

    public List<WinResponse> listWinsByUser(Long userId) {
        if (!userRepo.existsById(userId))
            throw new IllegalArgumentException("user not found");
        return winRepo.findByUser_UserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WinResponse getWinById(Long winId, Long userId) {
        Win win = winRepo.findByWinIdAndUser_UserId(winId, userId)
                .orElseThrow(() -> new IllegalArgumentException("win not found"));
        return toResponse(win);
    }

    @Transactional
    public void deleteWin(Long winId, Long userId) {
        Win win = winRepo.findByWinIdAndUser_UserId(winId, userId)
                .orElseThrow(() -> new IllegalArgumentException("win not found"));

        // Subtract trophies from user to keep totals consistent
        User user = win.getUser();
        int current = user.getTrophies() == null ? 0 : user.getTrophies();
        int toSubtract = win.getNumTrophies() == null ? 0 : win.getNumTrophies();
        user.setTrophies(Math.max(0, current - toSubtract));
        userRepo.save(user);

        winRepo.delete(win);
    }
}
