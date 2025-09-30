// // src/main/java/com/project/thelittlethings/controller/DevAuthController.java
// package com.project.thelittlethings.controller;

// import com.project.thelittlethings.entities.User;
// import com.project.thelittlethings.dto.users.AuthResponse;
// import com.project.thelittlethings.repositories.UserRepository;
// import com.project.thelittlethings.security.TokenUtil;
// import org.springframework.context.annotation.Profile;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// @Profile("dev") // only active when spring.profiles.active=dev
// @RestController
// @RequestMapping("/api/dev")
// public class DevAuthController {

//     private final UserRepository userRepo;

//     public DevAuthController(UserRepository userRepo) {
//         this.userRepo = userRepo;
//     }

//     // POST /api/dev/impersonate/123  -> returns { token, userId, username }
//     @PostMapping("/impersonate/{userId}")
//     public ResponseEntity<?> impersonate(@PathVariable Long userId) {
//         User u = userRepo.findById(userId).orElse(null);
//         if (u == null) return ResponseEntity.notFound().build();

//         // issue a normal JWT for this user's username (valid 7 days here)
//         String token = TokenUtil.issueToken(u.getUsername(), 60 * 60 * 24 * 7);
//         return ResponseEntity.ok(new AuthResponse(token, u.getUserId(), u.getUsername()));
//     }
// }
