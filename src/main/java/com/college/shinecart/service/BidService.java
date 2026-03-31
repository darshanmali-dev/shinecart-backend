package com.college.shinecart.service;

import com.college.shinecart.dto.BidDTO;
import com.college.shinecart.dto.BidUpdateMessage;
import com.college.shinecart.dto.PlaceBidRequest;
import com.college.shinecart.entity.Auction;
import com.college.shinecart.entity.AuctionStatus;
import com.college.shinecart.entity.Bid;
import com.college.shinecart.entity.User;
import com.college.shinecart.repository.AuctionRepository;
import com.college.shinecart.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BidService {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Place a bid on an auction
     */
    @Transactional
    public BidDTO placeBid(PlaceBidRequest request, User user) {
        // Get auction
        Auction auction = auctionRepository.findById(request.getAuctionId())
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Validate auction is active
        if (!auction.isActive()) {
            throw new RuntimeException("Auction is not active. Current status: " + auction.getStatus());
        }

        // Validate auction hasn't ended
        if (LocalDateTime.now().isAfter(auction.getEndTime())) {
            throw new RuntimeException("Auction has already ended");
        }

        // Validate bid amount
        BigDecimal minimumBid = auction.getCurrentPrice().add(auction.getBidIncrement());
        if (request.getAmount().compareTo(minimumBid) < 0) {
            throw new RuntimeException("Bid must be at least ₹" + minimumBid +
                    " (Current price: ₹" + auction.getCurrentPrice() +
                    " + Increment: ₹" + auction.getBidIncrement() + ")");
        }

        // Check if user is bidding on own auction (if they're the creator)
        if (auction.getCreatedBy() != null && auction.getCreatedBy().getId().equals(user.getId())) {
            throw new RuntimeException("You cannot bid on your own auction");
        }

        // Mark previous winning bid as not winning
        bidRepository.findByAuctionIdAndIsWinningTrue(auction.getId())
                .ifPresent(previousBid -> {
                    previousBid.setIsWinning(false);
                    bidRepository.save(previousBid);
                });

        // Create new bid
        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setAmount(request.getAmount());
        bid.setBidTime(LocalDateTime.now());
        bid.setIsWinning(true);

        // Check if bid extends auction time (last 2 minutes rule)
        LocalDateTime now = LocalDateTime.now();
        long minutesRemaining = Duration.between(now, auction.getEndTime()).toMinutes();

        boolean timeExtended = false;
        if (minutesRemaining < 2 && minutesRemaining >= 0) {
            auction.setEndTime(auction.getEndTime().plusMinutes(2));
            bid.setAutoExtend(true);
            timeExtended = true;
        }

        // Update auction
        auction.setCurrentPrice(request.getAmount());
        auction.setTotalBids(auction.getTotalBids() + 1);

        // Update status if needed
        if (minutesRemaining <= 5 && auction.getStatus() == AuctionStatus.ACTIVE) {
            auction.setStatus(AuctionStatus.ENDING_SOON);
        }

        // Save everything
        Bid savedBid = bidRepository.save(bid);
        auctionRepository.save(auction);

        // Send real-time update via WebSocket
        BidUpdateMessage message = new BidUpdateMessage();
        message.setAuctionId(auction.getId());
        message.setNewPrice(request.getAmount());
        message.setBidderName(user.getUsername());
        message.setBidTime(savedBid.getBidTime());
        message.setTotalBids(auction.getTotalBids());
        message.setTimeRemaining(calculateTimeRemaining(auction));
        message.setTimeExtended(timeExtended);

        messagingTemplate.convertAndSend("/topic/auction/" + auction.getId(), message);

        return convertToDTO(savedBid);
    }

    /**
     * Get bid history for an auction
     */
    public List<BidDTO> getBidHistory(Long auctionId) {
        return bidRepository.findByAuctionIdOrderByBidTimeDesc(auctionId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get user's bid history
     */
    public List<BidDTO> getUserBids(Long userId) {
        return bidRepository.findByUserIdOrderByBidTimeDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get current winning bid for an auction
     */
    public BidDTO getWinningBid(Long auctionId) {
        return bidRepository.findByAuctionIdAndIsWinningTrue(auctionId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    /**
     * Check if user has bid on auction
     */
    public boolean hasUserBid(Long auctionId, Long userId) {
        return bidRepository.existsByAuctionIdAndUserId(auctionId, userId);
    }

    /**
     * Get user's highest bid for an auction
     */
    public BigDecimal getUserMaxBid(Long auctionId, Long userId) {
        return bidRepository.findMaxBidByUserForAuction(auctionId, userId)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Get auctions where user is currently winning
     */
    public List<BidDTO> getUserWinningBids(Long userId) {
        return bidRepository.findByUserIdAndIsWinningTrueOrderByBidTimeDesc(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Convert Bid entity to DTO
     */
    private BidDTO convertToDTO(Bid bid) {
        BidDTO dto = new BidDTO();

        dto.setId(bid.getId());
        dto.setAuctionId(bid.getAuction().getId());
        dto.setAuctionTitle(bid.getAuction().getProduct().getName());
        dto.setUserId(bid.getUser().getId());
        dto.setUserName(bid.getUser().getUsername());
        dto.setAmount(bid.getAmount());
        dto.setBidTime(bid.getBidTime());
        dto.setIsWinning(bid.getIsWinning());
        dto.setAutoExtend(bid.getAutoExtend());

        return dto;
    }

    /**
     * Calculate time remaining in seconds
     */
    private Long calculateTimeRemaining(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        long seconds = Duration.between(now, auction.getEndTime()).getSeconds();
        return Math.max(0, seconds);
    }
}
