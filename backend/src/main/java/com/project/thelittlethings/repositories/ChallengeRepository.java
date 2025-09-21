package com.project.thelittlethings.repositories;

import com.project.thelittlethings.dto.challenges.ChallengeStatus;
import com.project.thelittlethings.entities.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // Find all challenges for a user with a given status
    List<Challenge> findAllByUser_UserIdAndStatus(Long userId, ChallengeStatus status);

    // Find all challenges for a user across multiple statuses
    List<Challenge> findAllByUser_UserIdAndStatusIn(Long userId, List<ChallengeStatus> statuses);

    // Find a specific challenge by ID, but only if it belongs to that user
    Optional<Challenge> findByIdAndUser_UserId(Long id, Long userId);

    // Check if user already has any challenge in one of these statuses
    boolean existsByUser_UserIdAndStatusIn(Long userId, List<ChallengeStatus> statuses);
}
