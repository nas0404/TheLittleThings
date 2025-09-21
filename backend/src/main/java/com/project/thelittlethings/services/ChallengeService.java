package com.project.thelittlethings.services;

import com.project.thelittlethings.dto.challenges.ChallengeResponse;
import com.project.thelittlethings.dto.challenges.ChallengeStatus;
import com.project.thelittlethings.dto.challenges.UpdateChallengeRequest;
import com.project.thelittlethings.entities.Challenge;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.ChallengeRepository;
import com.project.thelittlethings.repositories.UserRepository;
import com.project.thelittlethings.repositories.WinRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    private final ChallengeRepository challengeRepo;
    private final UserRepository userRepo;
    private final WinRepository winRepo; // used for generation from past wins

    // ---------- Queries ----------

    @Transactional(readOnly = true)
    public List<ChallengeResponse> listByUser(Long userId, String statusStr) {
        ChallengeStatus status = parseStatus(statusStr, ChallengeStatus.ACTIVE);
    return challengeRepo.findAllByUser_UserIdAndStatus(userId, status).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ChallengeResponse getAssigned(Long id, Long userId) {
    Challenge ch = challengeRepo.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
        return toDto(ch);
    }

    // ---------- Commands ----------

    @Transactional
    public ChallengeResponse accept(Long id, Long userId) {
        Challenge ch = requireOwned(id, userId);
        if (ch.getStatus() == ChallengeStatus.SUGGESTED) {
            ch.setStatus(ChallengeStatus.ACTIVE);
            ch.setUpdatedAt(Instant.now());
            challengeRepo.save(ch);
        }
        return toDto(ch);
    }

    @Transactional
    public void decline(Long id, Long userId) {
        Challenge ch = requireOwned(id, userId);
        if (ch.getStatus() == ChallengeStatus.SUGGESTED || ch.getStatus() == ChallengeStatus.ACTIVE) {
            ch.setStatus(ChallengeStatus.DECLINED);
            ch.setUpdatedAt(Instant.now());
            challengeRepo.save(ch);
        }
    }

    @Transactional
    public ChallengeResponse updateProgress(Long id, Long userId, UpdateChallengeRequest req) {
        Challenge ch = requireOwned(id, userId);
        if (ch.getStatus() != ChallengeStatus.ACTIVE) {
            throw new IllegalStateException("Only active challenges can be progressed");
        }

        if (req.getCurrent() != null) {
            ch.setProgressCurrent(Math.max(0, Math.min(req.getCurrent(), ch.getProgressTotal())));
        }
        if (req.getDelta() != null) {
            int next = ch.getProgressCurrent() + req.getDelta();
            ch.setProgressCurrent(Math.max(0, Math.min(next, ch.getProgressTotal())));
        }

        // auto-complete if reached total
        if (ch.getProgressCurrent() >= ch.getProgressTotal()) {
            ch.setStatus(ChallengeStatus.COMPLETED);
            // give trophies to user
            // user.addTrophies(ch.getReward());
        }

        ch.setUpdatedAt(Instant.now());
        challengeRepo.save(ch);
        return toDto(ch);
    }

    @Transactional
    public ChallengeResponse complete(Long id, Long userId) {
        Challenge ch = requireOwned(id, userId);
        if (ch.getStatus() != ChallengeStatus.ACTIVE && ch.getStatus() != ChallengeStatus.SUGGESTED) {
            return toDto(ch);
        }
        ch.setProgressCurrent(ch.getProgressTotal());
        ch.setStatus(ChallengeStatus.COMPLETED);
        ch.setUpdatedAt(Instant.now());
        // give trophies to user if not done yet
        challengeRepo.save(ch);
        return toDto(ch);
    }

    @Transactional
    public void archive(Long id, Long userId) {
        Challenge ch = requireOwned(id, userId);
        ch.setStatus(ChallengeStatus.ARCHIVED);
        ch.setUpdatedAt(Instant.now());
        challengeRepo.save(ch);
    }

    /**
     * Generate (or refresh) challenges based on the user's past wins.
     * Keep this simple as a baseline: one weekly challenge that nudges the user
     * toward their most frequent win category.
     */
    @Transactional
    public List<ChallengeResponse> generateFromWins(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Very basic heuristic: use count of wins to propose a challenge.
        // Replace with your real logic (e.g., top category in last 30 days).
    Instant thirtyDaysAgoInstant = Instant.now().minus(30, ChronoUnit.DAYS);
    java.time.OffsetDateTime thirtyDaysAgo = java.time.OffsetDateTime.ofInstant(thirtyDaysAgoInstant, java.time.ZoneOffset.UTC);
    long recentWins = winRepo.countRecentWinsForUser(userId, thirtyDaysAgo);
        
        int goal = Math.max(3, Math.min(10, (int) recentWins + 2)); // nudge slightly above recent behaviour


        // Upsert a suggested challenge if none active/suggested
    boolean exists = challengeRepo.existsByUser_UserIdAndStatusIn(
                userId,
                List.of(ChallengeStatus.SUGGESTED, ChallengeStatus.ACTIVE)
        );
        if (!exists) {
            Challenge ch = new Challenge();
            ch.setUser(user);
            ch.setTitle("Keep the streak going");
            ch.setDescription("Complete small wins inspired by your recent activity.");
            ch.setReward(Math.max(5, goal)); // simple reward baseline
            ch.setProgressCurrent(0);
            ch.setProgressTotal(goal);
            ch.setStatus(ChallengeStatus.SUGGESTED);
            ch.setSource("SYSTEM_FROM_WINS");
            ch.setCreatedAt(Instant.now());
            ch.setUpdatedAt(Instant.now());
            challengeRepo.save(ch);
        }

    return challengeRepo.findAllByUser_UserIdAndStatusIn(
                        userId,
                        List.of(ChallengeStatus.SUGGESTED, ChallengeStatus.ACTIVE))
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ---------- Helpers ----------

    private Challenge requireOwned(Long id, Long userId) {
    return challengeRepo.findByIdAndUser_UserId(id, userId)
                .orElseThrow(() -> new EntityNotFoundException("Challenge not found"));
    }

    private ChallengeStatus parseStatus(String raw, ChallengeStatus fallback) {
        try {
            return ChallengeStatus.valueOf(raw.toUpperCase());
        } catch (Exception e) {
            return fallback;
        }
    }

    private ChallengeResponse toDto(Challenge ch) {
        ChallengeResponse dto = new ChallengeResponse();
        dto.setId(ch.getId());
        dto.setTitle(ch.getTitle());
        dto.setDescription(ch.getDescription());
        dto.setReward(ch.getReward());
        dto.setProgressCurrent(ch.getProgressCurrent());
        dto.setProgressTotal(ch.getProgressTotal());
        dto.setStatus(ch.getStatus());
        dto.setCreatedAt(ch.getCreatedAt());
        dto.setUpdatedAt(ch.getUpdatedAt());
        dto.setSource(ch.getSource());
        return dto;
    }
}
