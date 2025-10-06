package com.project.thelittlethings.services;

import com.project.thelittlethings.entities.*;
import com.project.thelittlethings.repositories.*;
import com.project.thelittlethings.repositories.FriendChallengeRepository;
import com.project.thelittlethings.repositories.FriendshipRepository;
import com.project.thelittlethings.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepo;
    private final UserRepository userRepo;
    private final FriendChallengeRepository challengeRepo;

    // utility: canonical order (userA < userB)
    private User[] order(User u1, User u2) {
        return (u1.getUserId() < u2.getUserId()) ? new User[]{u1, u2} : new User[]{u2, u1};
    }

    private Friendship getOrThrow(Long id) {
        return friendshipRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
    }

    @Transactional
    public Friendship sendRequest(Long requesterId, Long targetId) {
        if (Objects.equals(requesterId, targetId)) throw new IllegalArgumentException("Cannot friend yourself");

        User requester = userRepo.findById(requesterId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User target    = userRepo.findById(targetId).orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        User[] pair = order(requester, target);
        Optional<Friendship> existing = friendshipRepo.findByUserAAndUserB(pair[0], pair[1]);

        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == Friendship.Status.ACCEPTED) throw new IllegalArgumentException("Already friends");
            if (f.getStatus() == Friendship.Status.PENDING)   throw new IllegalArgumentException("Request already pending");
            // revive from declined/canceled
            f.setStatus(Friendship.Status.PENDING);
            f.setRequestedBy(requester);
            f.setRespondedBy(null);
            f.setRespondedAt(null);
            f.setRequestedAt(OffsetDateTime.now());  // FIX: set timestamps
            // updatedAt will be set by @PreUpdate if you have it, but set explicitly too:
            f.setUpdatedAt(OffsetDateTime.now());
            return friendshipRepo.save(f);
        }

        try {
            Friendship f = Friendship.builder()
                    .userA(pair[0]).userB(pair[1])
                    .status(Friendship.Status.PENDING)
                    .requestedBy(requester)
                    .requestedAt(OffsetDateTime.now())  // FIX: set timestamps
                    .updatedAt(OffsetDateTime.now())
                    .build();
            return friendshipRepo.save(f);
        } catch (DataIntegrityViolationException ex) {
            // Race: another request inserted the same row first
            Friendship f = friendshipRepo.findByUserAAndUserB(pair[0], pair[1])
                    .orElseThrow(() -> ex);
            if (f.getStatus() == Friendship.Status.ACCEPTED) throw new IllegalArgumentException("Already friends");
            if (f.getStatus() == Friendship.Status.PENDING)  throw new IllegalArgumentException("Request already pending");
            // auto-reopen if previously declined/canceled
            f.setStatus(Friendship.Status.PENDING);
            f.setRequestedBy(requester);
            f.setRespondedBy(null);
            f.setRespondedAt(null);
            f.setRequestedAt(OffsetDateTime.now());
            f.setUpdatedAt(OffsetDateTime.now());
            return friendshipRepo.save(f);
        }
    }

    @Transactional
    public Friendship accept(Long meId, Long otherId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User other = userRepo.findById(otherId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User[] pair = order(me, other);

        Friendship f = friendshipRepo.findByUserAAndUserB(pair[0], pair[1])
                .orElseThrow(() -> new IllegalArgumentException("No request found"));

        if (f.getStatus() != Friendship.Status.PENDING) throw new IllegalArgumentException("Not pending");
        if (f.getRequestedBy().getUserId().equals(meId)) throw new IllegalArgumentException("Cannot accept your own request");

        f.setStatus(Friendship.Status.ACCEPTED);
        f.setRespondedBy(me);
        f.setRespondedAt(OffsetDateTime.now());
        f.setUpdatedAt(OffsetDateTime.now());
        return friendshipRepo.save(f);
    }

    @Transactional
    public Friendship decline(Long meId, Long otherId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User other = userRepo.findById(otherId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User[] pair = order(me, other);

        Friendship f = friendshipRepo.findByUserAAndUserB(pair[0], pair[1])
                .orElseThrow(() -> new IllegalArgumentException("No request found"));

        if (f.getStatus() != Friendship.Status.PENDING) throw new IllegalArgumentException("Not pending");
        if (f.getRequestedBy().getUserId().equals(meId)) throw new IllegalArgumentException("Requester cannot decline");

        f.setStatus(Friendship.Status.DECLINED);
        f.setRespondedBy(me);
        f.setRespondedAt(OffsetDateTime.now());
        f.setUpdatedAt(OffsetDateTime.now());
        return friendshipRepo.save(f);
    }

    @Transactional
    public Friendship cancel(Long meId, Long otherId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User other = userRepo.findById(otherId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User[] pair = order(me, other);

        Friendship f = friendshipRepo.findByUserAAndUserB(pair[0], pair[1])
                .orElseThrow(() -> new IllegalArgumentException("No request found"));

        if (f.getStatus() != Friendship.Status.PENDING) throw new IllegalArgumentException("Not pending");
        if (!f.getRequestedBy().getUserId().equals(meId)) throw new IllegalArgumentException("Only requester can cancel");

        f.setStatus(Friendship.Status.CANCELED);
        f.setRespondedBy(me);
        f.setRespondedAt(OffsetDateTime.now());
        f.setUpdatedAt(OffsetDateTime.now());
        return friendshipRepo.save(f);
    }

    @Transactional
    public void remove(Long meId, Long friendId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User other = userRepo.findById(friendId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User[] pair = order(me, other);

        Friendship f = friendshipRepo.findByUserAAndUserB(pair[0], pair[1])
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
        if (f.getStatus() != Friendship.Status.ACCEPTED) throw new IllegalArgumentException("Not friends");
        friendshipRepo.delete(f);
    }

    public List<Friendship> listAccepted(Long meId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendshipRepo.findByUserAOrUserBAndStatus(me, me, Friendship.Status.ACCEPTED);
    }

    public List<Friendship> pendingIncoming(Long meId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return friendshipRepo.findByStatusAndRequestedByNotAndUserAOrUserB(
                Friendship.Status.PENDING, me, me, me);
    }

    @Transactional
    public Friendship sendRequestByUsername(Long requesterId, String targetUsername) {
        if (targetUsername == null || targetUsername.isBlank())
            throw new IllegalArgumentException("Username required");

        User requester = userRepo.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        User target = userRepo.findByUsernameIgnoreCase(targetUsername.trim())
            .orElseThrow(() -> new IllegalArgumentException("Target user not found"));

        if (Objects.equals(requester.getUserId(), target.getUserId()))
            throw new IllegalArgumentException("Cannot friend yourself");

        User[] pair = order(requester, target);

        Optional<Friendship> existing = friendshipRepo.findByUserAAndUserB(pair[0], pair[1]);
        if (existing.isPresent()) {
            Friendship f = existing.get();
            if (f.getStatus() == Friendship.Status.ACCEPTED) throw new IllegalArgumentException("Already friends");
            if (f.getStatus() == Friendship.Status.PENDING)   throw new IllegalArgumentException("Request already pending");

            f.setStatus(Friendship.Status.PENDING);
            f.setRequestedBy(requester);
            f.setRespondedBy(null);
            f.setRespondedAt(null);
            f.setRequestedAt(OffsetDateTime.now());  // FIX: set timestamps
            f.setUpdatedAt(OffsetDateTime.now());
            return friendshipRepo.save(f);
        }

        try {
            Friendship f = Friendship.builder()
                .userA(pair[0]).userB(pair[1])
                .status(Friendship.Status.PENDING)
                .requestedBy(requester)
                .requestedAt(OffsetDateTime.now())  // FIX: set timestamps
                .updatedAt(OffsetDateTime.now())
                .build();
            return friendshipRepo.save(f);
        } catch (DataIntegrityViolationException ex) {
            Friendship f = friendshipRepo.findByUserAAndUserB(pair[0], pair[1])
                .orElseThrow(() -> ex);
            if (f.getStatus() == Friendship.Status.ACCEPTED) throw new IllegalArgumentException("Already friends");
            if (f.getStatus() == Friendship.Status.PENDING)  throw new IllegalArgumentException("Request already pending");
            f.setStatus(Friendship.Status.PENDING);
            f.setRequestedBy(requester);
            f.setRespondedBy(null);
            f.setRespondedAt(null);
            f.setRequestedAt(OffsetDateTime.now());
            f.setUpdatedAt(OffsetDateTime.now());
            return friendshipRepo.save(f);
        }
    }

    /* ---------------- Challenges ---------------- */

    private boolean areFriends(User u1, User u2) {
        User[] pair = order(u1, u2);
        return friendshipRepo.findByUserAAndUserB(pair[0], pair[1])
                .filter(f -> f.getStatus() == Friendship.Status.ACCEPTED)
                .isPresent();
    }

    @Transactional
    public FriendChallenge createChallenge(Long challengerId, Long opponentId,
                                        String goalList, LocalDate start,
                                        LocalDate end, Integer stake) {
        if (Objects.equals(challengerId, opponentId)) throw new IllegalArgumentException("Cannot challenge yourself");
        User c = userRepo.findById(challengerId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        User o = userRepo.findById(opponentId).orElseThrow(() -> new IllegalArgumentException("Opponent not found"));
        if (!areFriends(c, o)) throw new IllegalArgumentException("Users are not friends");

        int s = java.util.Optional.ofNullable(stake).orElse(0);
        if (s < 0) throw new IllegalArgumentException("Stake cannot be negative");
        if (trophiesOf(c) < s) throw new IllegalArgumentException("Insufficient trophies for stake");

        var now = java.time.OffsetDateTime.now();

        FriendChallenge fc = FriendChallenge.builder()
                .challenger(c).opponent(o)
                .goalList(goalList)
                .startDate(start).endDate(end)
                .trophiesStake(s)
                .status(FriendChallenge.Status.PROPOSED)
                .createdAt(now)
                .updatedAt(now)
                .escrowed(false)
                .build();

        return challengeRepo.save(fc);
    }


    @Transactional
    public FriendChallenge acceptChallenge(Long meId, Long challengeId) {
        FriendChallenge fc = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        if (!fc.getOpponent().getUserId().equals(meId))
            throw new IllegalArgumentException("Only opponent can accept");
        if (fc.getStatus() != FriendChallenge.Status.PROPOSED)
            throw new IllegalArgumentException("Wrong state");

        User c = fc.getChallenger();
        User o = fc.getOpponent();
        int s = java.util.Optional.ofNullable(fc.getTrophiesStake()).orElse(0);

        // both must be able to pay the stake now
        if (trophiesOf(c) < s) throw new IllegalArgumentException("Challenger has insufficient trophies now");
        if (trophiesOf(o) < s) throw new IllegalArgumentException("You have insufficient trophies to accept");

        // escrow: deduct from both
        addTrophies(c, -s);
        addTrophies(o, -s);
        userRepo.save(c);
        userRepo.save(o);

        fc.setStatus(FriendChallenge.Status.ACCEPTED);
        fc.setEscrowed(true);
        fc.setUpdatedAt(java.time.OffsetDateTime.now());
        return challengeRepo.save(fc);
    }

    @Transactional
    public FriendChallenge declineChallenge(Long meId, Long challengeId) {
        FriendChallenge fc = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        if (!fc.getOpponent().getUserId().equals(meId))
            throw new IllegalArgumentException("Only opponent can decline");
        if (fc.getStatus() != FriendChallenge.Status.PROPOSED)
            throw new IllegalArgumentException("Wrong state");

        fc.setStatus(FriendChallenge.Status.DECLINED);
        fc.setUpdatedAt(java.time.OffsetDateTime.now());
        return challengeRepo.save(fc);
    }

    @Transactional
    public FriendChallenge completeChallenge(Long winnerId, Long challengeId) {
        FriendChallenge fc = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        if (fc.getStatus() != FriendChallenge.Status.ACCEPTED && fc.getStatus() != FriendChallenge.Status.ACTIVE)
            throw new IllegalArgumentException("Challenge not active");

        User winner = userRepo.findById(winnerId).orElseThrow(() -> new IllegalArgumentException("Winner not found"));
        if (!winner.getUserId().equals(fc.getChallenger().getUserId())
                && !winner.getUserId().equals(fc.getOpponent().getUserId()))
            throw new IllegalArgumentException("Winner must be a participant");

        fc.setStatus(FriendChallenge.Status.COMPLETED);
        fc.setWinner(winner);

        // example trophy transfer
        if (fc.getTrophiesStake() != null && fc.getTrophiesStake() > 0) {
            int stake = fc.getTrophiesStake();
            User loser = winner.getUserId().equals(fc.getChallenger().getUserId()) ? fc.getOpponent() : fc.getChallenger();
            winner.setTrophies(Optional.ofNullable(winner.getTrophies()).orElse(0) + stake);
            loser.setTrophies(Math.max(0, Optional.ofNullable(loser.getTrophies()).orElse(0) - stake));
            userRepo.save(winner);
            userRepo.save(loser);
        }

        return challengeRepo.save(fc);
    }

    public List<FriendChallenge> proposedChallengesFor(Long meId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return challengeRepo.findByOpponentAndStatus(me, FriendChallenge.Status.PROPOSED);
    }

    public List<FriendChallenge> myChallenges(Long meId) {
        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        var statuses = List.of(
            FriendChallenge.Status.PROPOSED,
            FriendChallenge.Status.ACCEPTED,
            FriendChallenge.Status.ACTIVE,
            FriendChallenge.Status.COMPLETION_REQUESTED
        );
        return challengeRepo.findByChallengerOrOpponentAndStatusIn(me, me, statuses);
    }

    
    @Transactional
    public FriendChallenge requestCompletion(Long meId, Long challengeId) {
        FriendChallenge fc = challengeRepo.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));
        if (!Objects.equals(fc.getChallenger().getUserId(), meId) &&
            !Objects.equals(fc.getOpponent().getUserId(), meId))
            throw new IllegalArgumentException("Not a participant");

        if (fc.getStatus() != FriendChallenge.Status.ACCEPTED &&
            fc.getStatus() != FriendChallenge.Status.ACTIVE)
            throw new IllegalArgumentException("Challenge not active");

        User me = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        fc.setStatus(FriendChallenge.Status.COMPLETION_REQUESTED);
        fc.setCompletionRequestedBy(me);
        fc.setCompletionRequestedAt(java.time.OffsetDateTime.now());
        fc.setUpdatedAt(java.time.OffsetDateTime.now());
        return challengeRepo.save(fc);
    }


    @Transactional
    public FriendChallenge confirmCompletion(Long meId, Long challengeId) {
        FriendChallenge fc = challengeRepo.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        if (fc.getStatus() != FriendChallenge.Status.COMPLETION_REQUESTED)
            throw new IllegalArgumentException("No completion to confirm");
        if (fc.getCompletionRequestedBy() == null)
            throw new IllegalStateException("Requestor missing");

        Long requesterId = fc.getCompletionRequestedBy().getUserId();
        boolean meIsParticipant = Objects.equals(fc.getChallenger().getUserId(), meId)
                            || Objects.equals(fc.getOpponent().getUserId(), meId);
        if (!meIsParticipant) throw new IllegalArgumentException("Not a participant");
        if (Objects.equals(requesterId, meId))
            throw new IllegalArgumentException("Requester cannot confirm");

        // Winner = confirmer (you can invert if you prefer)
        User winner = userRepo.findById(meId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        int stake = java.util.Optional.ofNullable(fc.getTrophiesStake()).orElse(0);
        int pool  = fc.isEscrowed() ? stake * 2 : 0; // if escrow didn’t happen, don’t double pay

        if (pool > 0) {
            addTrophies(winner, pool);
            userRepo.save(winner);
            fc.setEscrowed(false); // consumed
        }

        fc.setStatus(FriendChallenge.Status.COMPLETED);
        fc.setWinner(winner);
        fc.setCompletionConfirmedBy(winner);
        fc.setCompletionConfirmedAt(java.time.OffsetDateTime.now());
        fc.setUpdatedAt(java.time.OffsetDateTime.now());
        return challengeRepo.save(fc);
    }


    @Transactional
    public FriendChallenge rejectCompletion(Long meId, Long challengeId) {
        FriendChallenge fc = challengeRepo.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found"));

        if (fc.getStatus() != FriendChallenge.Status.COMPLETION_REQUESTED)
            throw new IllegalArgumentException("No completion to reject");
        if (fc.getCompletionRequestedBy() == null)
            throw new IllegalStateException("Requestor missing");
        if (Objects.equals(fc.getCompletionRequestedBy().getUserId(), meId))
            throw new IllegalArgumentException("Requester cannot reject");

        fc.setStatus(FriendChallenge.Status.ACCEPTED); // or ACTIVE if you use it
        fc.setCompletionRequestedBy(null);
        fc.setCompletionRequestedAt(null);
        fc.setUpdatedAt(java.time.OffsetDateTime.now());
        return challengeRepo.save(fc);
    }


    private int trophiesOf(User u) {
        return java.util.Optional.ofNullable(u.getTrophies()).orElse(0);
    }
    
    private void addTrophies(User u, int delta) {
        u.setTrophies(Math.max(0, trophiesOf(u) + delta));
    }
}
