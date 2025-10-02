package com.project.thelittlethings.services;

import com.project.thelittlethings.entities.FriendChallenge;
import com.project.thelittlethings.entities.Friendship;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.repositories.FriendChallengeRepository;
import com.project.thelittlethings.repositories.FriendshipRepository;
import com.project.thelittlethings.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests cover:
 * 1) sending a friend request (PENDING)
 * 2) challenge staking flow: accept -> escrow -> request completion -> confirm -> winner gets pool
 */
class FriendServiceTest {

    private FriendshipRepository friendshipRepo;
    private UserRepository userRepo;
    private FriendChallengeRepository challengeRepo;
    private FriendService service;

    @BeforeEach
    void setup() {
        friendshipRepo = mock(FriendshipRepository.class);
        userRepo = mock(UserRepository.class);
        challengeRepo = mock(FriendChallengeRepository.class);
        service = new FriendService(friendshipRepo, userRepo, challengeRepo);
    }

    private User user(long id, String username, int trophies) {
        User u = new User();
        u.setUserId(id);
        u.setUsername(username);
        u.setTrophies(trophies);
        return u;
    }

    /* ---------------------------------------------------------------------- */
    /* 1) Friend request creates PENDING friendship                           */
    /* ---------------------------------------------------------------------- */
    @Test
    void sendRequest_createsPendingFriendship() {
        User alice = user(1L, "alice", 0);
        User bob   = user(2L, "bob",   0);

        when(userRepo.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepo.findById(2L)).thenReturn(Optional.of(bob));

        // no existing friendship for (1,2)
        when(friendshipRepo.findByUserAAndUserB(any(User.class), any(User.class)))
            .thenReturn(Optional.empty());

        // when saving, return the same entity but with an id + timestamps set
        when(friendshipRepo.save(any(Friendship.class))).thenAnswer(inv -> {
            Friendship f = inv.getArgument(0);
            f.setId(99L);
            if (f.getRequestedAt() == null) f.setRequestedAt(OffsetDateTime.now());
            if (f.getUpdatedAt() == null)   f.setUpdatedAt(OffsetDateTime.now());
            return f;
        });

        Friendship created = service.sendRequest(1L, 2L);

        assertNotNull(created.getId());
        assertEquals(Friendship.Status.PENDING, created.getStatus());
        assertEquals(alice.getUserId(), created.getRequestedBy().getUserId());
        // canonical order (userA < userB)
        assertEquals(1L, created.getUserA().getUserId());
        assertEquals(2L, created.getUserB().getUserId());
        assertNotNull(created.getRequestedAt());
        assertNotNull(created.getUpdatedAt());

        verify(friendshipRepo).save(any(Friendship.class));
    }

    /* ---------------------------------------------------------------------- */
    /* 2) Challenge escrow + completion payout to winner                      */
    /* ---------------------------------------------------------------------- */
    
    @Test
    void challenge_acceptEscrowsStake_andConfirmPaysWinnerPool() {
        // Given: challenger has 200, opponent has 150 trophies, stake = 50
        User challenger = user(10L, "alice", 200);
        User opponent   = user(20L, "bob",   150);

        // Proposed challenge
        FriendChallenge fc = FriendChallenge.builder()
            .id(7L)
            .challenger(challenger)
            .opponent(opponent)
            .goalList("Test goal")
            .trophiesStake(50)
            .status(FriendChallenge.Status.PROPOSED)
            .createdAt(OffsetDateTime.now())
            .updatedAt(OffsetDateTime.now())
            .escrowed(false)
            .build();

        when(challengeRepo.findById(7L)).thenReturn(Optional.of(fc));

        // user lookups used by service
        when(userRepo.findById(10L)).thenReturn(Optional.of(challenger));
        when(userRepo.findById(20L)).thenReturn(Optional.of(opponent));

        // pass-through saves
        when(challengeRepo.save(any(FriendChallenge.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        /* 1) Opponent accepts -> both pay 50, escrowed=true, status=ACCEPTED */
        FriendChallenge afterAccept = service.acceptChallenge(20L, 7L);

        assertEquals(FriendChallenge.Status.ACCEPTED, afterAccept.getStatus());
        assertTrue(afterAccept.isEscrowed());
        assertEquals(150, challenger.getTrophies()); // 200 - 50
        assertEquals(100, opponent.getTrophies());   // 150 - 50

        /* 2) Challenger requests completion -> status=COMPLETION_REQUESTED */
        FriendChallenge afterReq = service.requestCompletion(10L, 7L);
        assertEquals(FriendChallenge.Status.COMPLETION_REQUESTED, afterReq.getStatus());
        assertNotNull(afterReq.getCompletionRequestedBy());

        /* 3) Opponent confirms -> opponent is winner, gets pool 100 (2*50) */
        FriendChallenge afterConfirm = service.confirmCompletion(20L, 7L);

        assertEquals(FriendChallenge.Status.COMPLETED, afterConfirm.getStatus());
        assertNotNull(afterConfirm.getWinner());
        assertEquals(20L, afterConfirm.getWinner().getUserId());
        assertFalse(afterConfirm.isEscrowed(), "escrow should be consumed");

        // Challenger stayed at 150; Opponent had 100, receives +100 pool => 200
        assertEquals(150, challenger.getTrophies());
        assertEquals(200, opponent.getTrophies());

        // repo interactions
        verify(challengeRepo, atLeastOnce()).save(any(FriendChallenge.class));
        verify(userRepo, atLeast(2)).save(any(User.class)); // once on accept (two saves), once on confirm (winner)
    }
}
