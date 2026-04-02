package com.college.shinecart.controller;

import com.college.shinecart.dto.AuctionDTO;
import com.college.shinecart.dto.CreateAuctionRequest;
import com.college.shinecart.dto.UpdateAuctionRequest;
import com.college.shinecart.entity.User;
import com.college.shinecart.repository.UserRepository;
import com.college.shinecart.service.AuctionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin/auctions")
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class AdminAuctionController {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> debug = new HashMap<>();
        debug.put("authenticated", auth.isAuthenticated());
        debug.put("name", auth.getName());
        debug.put("authorities", auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList()));
        debug.put("principal", auth.getPrincipal().getClass().getName());

        return ResponseEntity.ok(debug);
    }
    /**
     * Create a new auction
     * POST /api/admin/auctions
     */
    @PostMapping
    public ResponseEntity<?> createAuction(@Valid @RequestBody CreateAuctionRequest request) {
        try {
            User admin = getAuthenticatedUser();
            AuctionDTO auction = auctionService.createAuction(request, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Auction created successfully!");
            response.put("auction", auction);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get ALL auctions (with optional filters)
     * GET /api/admin/auctions?status=ACTIVE&search=ring
     */
    @GetMapping
    public ResponseEntity<List<AuctionDTO>> getAllAuctions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search
    ) {
        List<AuctionDTO> auctions = auctionService.getAuctionsForAdmin(status, search);
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get auctions created by current admin
     * GET /api/admin/auctions/my-auctions
     */
    @GetMapping("/my-auctions")
    public ResponseEntity<List<AuctionDTO>> getMyAuctions() {
        User admin = getAuthenticatedUser();
        List<AuctionDTO> auctions = auctionService.getAuctionsByCreator(admin.getId());
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get single auction details
     * GET /api/admin/auctions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAuctionById(@PathVariable Long id) {
        try {
            AuctionDTO auction = auctionService.getAuctionById(id);
            return ResponseEntity.ok(auction);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Update an auction (only UPCOMING auctions)
     * PUT /api/admin/auctions/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAuction(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAuctionRequest request
    ) {
        try {
            User admin = getAuthenticatedUser();
            AuctionDTO auction = auctionService.updateAuction(id, request, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Auction updated successfully!");
            response.put("auction", auction);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Delete an auction (only UPCOMING or CANCELLED)
     * DELETE /api/admin/auctions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAuction(@PathVariable Long id) {
        try {
            User admin = getAuthenticatedUser();
            auctionService.deleteAuction(id, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Auction deleted successfully");

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Cancel an auction
     * PUT /api/admin/auctions/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAuction(@PathVariable Long id) {
        try {
            User admin = getAuthenticatedUser();
            AuctionDTO auction = auctionService.cancelAuction(id, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Auction cancelled successfully");
            response.put("auction", auction);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Extend auction end time
     * PUT /api/admin/auctions/{id}/extend?minutes=30
     */
    @PutMapping("/{id}/extend")
    public ResponseEntity<?> extendAuction(
            @PathVariable Long id,
            @RequestParam(defaultValue = "30") int minutes
    ) {
        try {
            User admin = getAuthenticatedUser();
            AuctionDTO auction = auctionService.extendAuction(id, minutes, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Auction extended by " + minutes + " minutes");
            response.put("auction", auction);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Manually end an auction early
     * PUT /api/admin/auctions/{id}/end
     */
    @PutMapping("/{id}/end")
    public ResponseEntity<?> endAuction(@PathVariable Long id) {
        try {
            User admin = getAuthenticatedUser();
            AuctionDTO auction = auctionService.endAuctionManually(id, admin);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Auction ended successfully");
            response.put("auction", auction);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get auction statistics
     * GET /api/admin/auctions/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getAuctionStatistics() {
        return ResponseEntity.ok(auctionService.getAuctionStatistics());
    }

    /**
     * Helper method to get authenticated user
     */
    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String name = authentication.getName();
        return userRepository.findByUsername(name)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}