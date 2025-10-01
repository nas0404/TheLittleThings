package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.FriendChallenge;
import com.project.thelittlethings.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendChallengeRepository extends JpaRepository<FriendChallenge, Long> {
    List<FriendChallenge> findByChallengerOrOpponent(User c, User o);
    List<FriendChallenge> findByOpponentAndStatus(User opponent, FriendChallenge.Status status);
    List<FriendChallenge> findByChallengerOrOpponentAndStatusIn(User challenger, User opponent, List<FriendChallenge.Status> statuses);
}
