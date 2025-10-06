package com.project.thelittlethings.repositories;

import com.project.thelittlethings.entities.Friendship;
import com.project.thelittlethings.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    // canonical pair search
    Optional<Friendship> findByUserAAndUserB(User userA, User userB);

    List<Friendship> findByUserAOrUserBAndStatus(User a, User b, Friendship.Status status);

    // pending for me (incoming)
    List<Friendship> findByStatusAndRequestedByNotAndUserAOrUserB(
            Friendship.Status status, User requestedByNot, User a, User b);
}
