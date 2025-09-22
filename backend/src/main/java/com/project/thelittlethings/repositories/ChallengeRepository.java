package com.project.thelittlethings.repositories;

import com.project.thelittlethings.dto.challenges.ChallengeStatus;
import com.project.thelittlethings.entities.Challenge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findAllByUser_UserIdAndStatus(Long userId, ChallengeStatus status);

    List<Challenge> findAllByUser_UserIdAndStatusIn(Long userId, List<ChallengeStatus> statuses);

    Optional<Challenge> findByIdAndUser_UserId(Long id, Long userId);

    boolean existsByUser_UserIdAndStatusIn(Long userId, List<ChallengeStatus> statuses);
}
