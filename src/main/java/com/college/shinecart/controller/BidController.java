package com.college.shinecart.controller;

import com.college.shinecart.dto.BidDTO;
import com.college.shinecart.dto.PlaceBidRequest;
import com.college.shinecart.entity.User;
import com.college.shinecart.repository.UserRepository;
import com.college.shinecart.service.BidService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bids")
@CrossOrigin(origins = "http://localhost:5173")
public class BidController {

    @Autowired
    private BidService bidService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Place a bid on an auction
     * POST /api/bids
     */
    @PostMapping
    public ResponseEntity<?> placeBid(@Valid @RequestBody PlaceBidRequest request) {
        try {
            // Get authenticated user
            User user = getAuthenticatedUser();

            BidDTO bid = bidService.placeBid(request, user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bid placed successfully!");
            response.put("bid", bid);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get bid history for an auction
     * GET /api/bids/auction/{auctionId}
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<List<BidDTO>> getBidHistory(@PathVariable Long auctionId) {
        List<BidDTO> bids = bidService.getBidHistory(auctionId);
        return ResponseEntity.ok(bids);
    }

    /**
     * Get current user's bid history
     * GET /api/bids/my-bids
     */
    @GetMapping("/my-bids")
    public ResponseEntity<List<BidDTO>> getMyBids() {
        User user = getAuthenticatedUser();
        List<BidDTO> bids = bidService.getUserBids(user.getId());
        return ResponseEntity.ok(bids);
    }

    /**
     * Get user's bid history by user ID
     * GET /api/bids/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BidDTO>> getUserBids(@PathVariable Long userId) {
        List<BidDTO> bids = bidService.getUserBids(userId);
        return ResponseEntity.ok(bids);
    }

    /**
     * Get current winning bid for an auction
     * GET /api/bids/auction/{auctionId}/winning
     */
    @GetMapping("/auction/{auctionId}/winning")
    public ResponseEntity<?> getWinningBid(@PathVariable Long auctionId) {
        BidDTO bid = bidService.getWinningBid(auctionId);
        if (bid != null) {
            return ResponseEntity.ok(bid);
        } else {
            return ResponseEntity.ok(Map.of("message", "No bids yet"));
        }
    }

    /**
     * Check if user has bid on auction
     * GET /api/bids/check/{auctionId}
     */
    @GetMapping("/check/{auctionId}")
    public ResponseEntity<Map<String, Object>> hasUserBid(@PathVariable Long auctionId) {
        User user = getAuthenticatedUser();
        boolean hasBid = bidService.hasUserBid(auctionId, user.getId());
        BigDecimal maxBid = bidService.getUserMaxBid(auctionId, user.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("hasBid", hasBid);
        response.put("maxBid", maxBid);

        return ResponseEntity.ok(response);
    }

    /**
     * Get auctions where current user is winning
     * GET /api/bids/winning
     */
    @GetMapping("/winning")
    public ResponseEntity<List<BidDTO>> getMyWinningBids() {
        User user = getAuthenticatedUser();
        List<BidDTO> bids = bidService.getUserWinningBids(user.getId());
        return ResponseEntity.ok(bids);
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