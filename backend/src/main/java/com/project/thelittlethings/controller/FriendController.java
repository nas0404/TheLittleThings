package com.project.thelittlethings.controller;

import com.project.thelittlethings.dto.friends.*;
import com.project.thelittlethings.entities.FriendChallenge;
import com.project.thelittlethings.entities.Friendship;
import com.project.thelittlethings.entities.User;
import com.project.thelittlethings.security.HMACtokens;
import com.project.thelittlethings.services.FriendService;
import com.project.thelittlethings.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    private Long me(String auth) {
        String token = auth.replaceFirst("Bearer ", "");
        if (!HMACtokens.validateToken(token) || userService.isTokenBlacklisted(token))
            throw new IllegalArgumentException("Unauthorized");
        String username = HMACtokens.extractUsername(token);
        User u = userService.findByUsername(username);
        if (u == null) throw new IllegalArgumentException("User not found");
        return u.getUserId();
    }

    /* -------- friendships -------- */

    @PostMapping("/requests")
    public ResponseEntity<?> send(@RequestHeader("Authorization") String auth, @RequestBody FriendRequestDto dto) {
        try {
            Long me = me(auth);
            Friendship f = friendService.sendRequest(me, dto.getTargetUserId());
            return ResponseEntity.ok(toResponse(me, f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/requests/{otherUserId}/accept")
    public ResponseEntity<?> accept(@RequestHeader("Authorization") String auth, @PathVariable Long otherUserId) {
        try {
            Long me = me(auth);
            Friendship f = friendService.accept(me, otherUserId);
            return ResponseEntity.ok(toResponse(me, f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/requests/{otherUserId}/decline")
    public ResponseEntity<?> decline(@RequestHeader("Authorization") String auth, @PathVariable Long otherUserId) {
        try {
            Long me = me(auth);
            Friendship f = friendService.decline(me, otherUserId);
            return ResponseEntity.ok(toResponse(me, f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/requests/{otherUserId}/cancel")
    public ResponseEntity<?> cancel(@RequestHeader("Authorization") String auth, @PathVariable Long otherUserId) {
        try {
            Long me = me(auth);
            Friendship f = friendService.cancel(me, otherUserId);
            return ResponseEntity.ok(toResponse(me, f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{friendUserId}")
    public ResponseEntity<?> remove(@RequestHeader("Authorization") String auth, @PathVariable Long friendUserId) {
        try {
            Long me = me(auth);
            friendService.remove(me, friendUserId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestHeader("Authorization") String auth) {
        try {
            Long me = me(auth);
            List<FriendshipResponse> out = friendService.listAccepted(me).stream()
                    .map(f -> toResponse(me, f)).toList();
            return ResponseEntity.ok(out);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    @GetMapping("/requests/incoming")
    public ResponseEntity<?> incoming(@RequestHeader("Authorization") String auth) {
        try {
            Long me = me(auth);
            List<FriendshipResponse> out = friendService.pendingIncoming(me).stream()
                    .map(f -> toResponse(me, f)).toList();
            return ResponseEntity.ok(out);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    private FriendshipResponse toResponse(Long meId, Friendship f) {
        User friend = f.getUserA().getUserId().equals(meId) ? f.getUserB() : f.getUserA();
        boolean outgoing = f.getRequestedBy().getUserId().equals(meId);
        return FriendshipResponse.builder()
                .id(f.getId())
                .friendId(friend.getUserId())
                .friendUsername(friend.getUsername())
                .status(f.getStatus().name().toLowerCase())
                .outgoing(outgoing)
                .requestedAt(f.getRequestedAt())
                .build();
    }

    @PostMapping("/requests/by-username")
    public ResponseEntity<?> sendByUsername(@RequestHeader("Authorization") String auth,
                                            @RequestBody FriendRequestByUsernameDto dto) {
        try {
            Long me = me(auth);
            Friendship f = friendService.sendRequestByUsername(me, dto.getUsername());
            return ResponseEntity.ok(toResponse(me, f));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* -------- challenges -------- */

    @PostMapping("/challenges")
    public ResponseEntity<?> createChallenge(@RequestHeader("Authorization") String auth,
                                             @RequestBody ChallengeCreateDto dto) {
        try {
            Long me = me(auth);
            FriendChallenge fc = friendService.createChallenge(
                    me, dto.getOpponentId(), dto.getGoalList(), dto.getStartDate(), dto.getEndDate(), dto.getTrophiesStake());
            return ResponseEntity.ok(toChallenge(fc));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/challenges/{id}/accept")
    public ResponseEntity<?> acceptChallenge(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        try {
            Long me = me(auth);
            FriendChallenge fc = friendService.acceptChallenge(me, id);
            return ResponseEntity.ok(toChallenge(fc));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/challenges/{id}/decline")
    public ResponseEntity<?> declineChallenge(@RequestHeader("Authorization") String auth, @PathVariable Long id) {
        try {
            Long me = me(auth);
            FriendChallenge fc = friendService.declineChallenge(me, id);
            return ResponseEntity.ok(toChallenge(fc));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/challenges/{id}/complete")
    public ResponseEntity<?> completeChallenge(@RequestHeader("Authorization") String auth,
                                               @PathVariable Long id,
                                               @RequestParam("winnerUserId") Long winnerUserId) {
        try {
            Long me = me(auth); // validates token
            FriendChallenge fc = friendService.completeChallenge(winnerUserId, id);
            return ResponseEntity.ok(toChallenge(fc));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private ChallengeResponse toChallenge(FriendChallenge fc) {
        return ChallengeResponse.builder()
                .id(fc.getId())
                .challengerId(fc.getChallenger().getUserId())
                .opponentId(fc.getOpponent().getUserId())
                .status(fc.getStatus().name().toLowerCase())
                .goalList(fc.getGoalList())
                .trophiesStake(fc.getTrophiesStake())
                .updatedAt(fc.getUpdatedAt())
                .build();
    }

    @GetMapping("/challenges/mine")
    public ResponseEntity<?> myChallenges(@RequestHeader("Authorization") String auth) {
        Long me = me(auth);
        var list = friendService.myChallenges(me).stream()
            .map(ChallengeResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/challenges/proposed")
    public ResponseEntity<?> proposedChallenges(@RequestHeader("Authorization") String auth) {
        Long me = me(auth);
        var list = friendService.proposedChallengesFor(me).stream()
            .map(ChallengeResponse::fromEntity)
            .toList();
        return ResponseEntity.ok(list);
    }
}
