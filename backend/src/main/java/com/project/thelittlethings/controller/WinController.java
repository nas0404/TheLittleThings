// package com.project.thelittlethings.controller;

// import com.project.thelittlethings.dto.wins.CreateWinRequest;
// import com.project.thelittlethings.dto.wins.UpdateWinRequest;
// import com.project.thelittlethings.dto.wins.WinResponse;
// import com.project.thelittlethings.services.WinService;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// import java.util.List;

// @RestController
// @RequestMapping("/api/wins")
// public class WinController {

//     private final WinService service;

//     public WinController(WinService service) {
//         this.service = service;
//     }

//     // Create a new Win
//     @PostMapping
//     public ResponseEntity<WinResponse> create(@RequestBody CreateWinRequest req) {
//         return ResponseEntity.ok(service.createWin(req));
//     }

//     // List all wins for a user
//     @GetMapping
//     public List<WinResponse> list(@RequestParam Long userId) {
//         return service.listWinsByUser(userId);
//     }

//     // Get single win (ownership check)
//     @GetMapping("/{id}")
//     public WinResponse get(@PathVariable Long id, @RequestParam Long userId) {
//         return service.getWinById(id, userId);
//     }

//     // Update a win
//     @PutMapping("/{id}")
//     public ResponseEntity<WinResponse> update(@PathVariable Long id,
//             @RequestParam Long userId,
//             @RequestBody UpdateWinRequest req) {
//         return ResponseEntity.ok(service.updateWin(id, userId, req));
//     }

//     // Delete a win
//     @DeleteMapping("/{id}")
//     public ResponseEntity<Void> delete(@PathVariable Long id, @RequestParam Long userId) {
//         service.deleteWin(id, userId);
//         return ResponseEntity.noContent().build();
//     }
// }