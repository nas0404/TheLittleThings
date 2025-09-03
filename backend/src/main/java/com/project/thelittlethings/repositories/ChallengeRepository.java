package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    // Find a specific challenge owned by a user
    Optional<Challenge> findByChallengeIdAndUser_UserId(Long challengeId, Long userId);

    // Check if a user has any challenges
    boolean existsByUser_UserId(Long userId);

    // Verify ownership: does this challenge belong to the given user?
    boolean existsByChallengeIdAndUser_UserId(Long challengeId, Long userId);
}
