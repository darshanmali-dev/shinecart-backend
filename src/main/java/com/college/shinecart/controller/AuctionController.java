package com.college.shinecart.controller;

import com.college.shinecart.dto.AuctionDTO;
import com.college.shinecart.service.AuctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auctions")
@CrossOrigin(origins = "https://shinecart-frontend.vercel.app")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    /**
     * Get all active auctions
     * GET /api/auctions/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<AuctionDTO>> getActiveAuctions() {
        List<AuctionDTO> auctions = auctionService.getActiveAuctions();
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get all upcoming auctions
     * GET /api/auctions/upcoming
     */
    @GetMapping("/upcoming")
    public ResponseEntity<List<AuctionDTO>> getUpcomingAuctions() {
        List<AuctionDTO> auctions = auctionService.getUpcomingAuctions();
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get all ended auctions
     * GET /api/auctions/ended
     */
    @GetMapping("/ended")
    public ResponseEntity<List<AuctionDTO>> getEndedAuctions() {
        List<AuctionDTO> auctions = auctionService.getEndedAuctions();
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get all auctions (all statuses)
     * GET /api/auctions
     */
    @GetMapping
    public ResponseEntity<List<AuctionDTO>> getAllAuctions() {
        List<AuctionDTO> auctions = auctionService.getAllAuctions();
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get auction by ID
     * GET /api/auctions/{id}
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
     * Get auctions by product ID
     * GET /api/auctions/product/{productId}
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<AuctionDTO>> getAuctionsByProduct(@PathVariable Long productId) {
        List<AuctionDTO> auctions = auctionService.getAuctionsByProduct(productId);
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get auctions won by user
     * GET /api/auctions/won/{userId}
     */
    @GetMapping("/won/{userId}")
    public ResponseEntity<List<AuctionDTO>> getAuctionsWonByUser(@PathVariable Long userId) {
        List<AuctionDTO> auctions = auctionService.getAuctionsWonByUser(userId);
        return ResponseEntity.ok(auctions);
    }

    /**
     * Get auction statistics
     * GET /api/auctions/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getAuctionStats() {
        return ResponseEntity.ok(auctionService.getAuctionStatistics());
    }
}