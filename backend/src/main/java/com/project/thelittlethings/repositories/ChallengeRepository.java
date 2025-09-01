package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // Find a specific challenge owned by a user
    Optional<Challenge> findByChallengeIdAndUserId(Long challengeId, Long userId);

    // Check if a user has any challenges
    boolean existsBy_UserId(Long userId);

    // Verify ownership: does this challenge belong to the given user?
    boolean existsByChallengeIdAndUserId(Long challengeId, Long userId);
}
