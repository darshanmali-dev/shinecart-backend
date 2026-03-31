package com.college.shinecart.scheduler;

import com.college.shinecart.dto.AuctionStatusMessage;
import com.college.shinecart.entity.Auction;
import com.college.shinecart.entity.AuctionStatus;
import com.college.shinecart.entity.Bid;
import com.college.shinecart.repository.AuctionRepository;
import com.college.shinecart.repository.BidRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class AuctionScheduler {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Check and update auction statuses every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // Run every 30 seconds
    @Transactional
    public void updateAuctionStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // Start upcoming auctions
        startUpcomingAuctions(now);

        // Mark auctions as ending soon (last 5 minutes)
        markAuctionsEndingSoon(now);

        // End expired auctions
        endExpiredAuctions(now);
    }

    /**
     * Start auctions that have reached their start time
     */
    private void startUpcomingAuctions(LocalDateTime now) {
        List<Auction> auctionsToStart = auctionRepository.findAuctionsToStart(now);

        for (Auction auction : auctionsToStart) {
            auction.setStatus(AuctionStatus.ACTIVE);
            auctionRepository.save(auction);

            System.out.println("Started auction: " + auction.getId() + " - " + auction.getProduct().getName());

            // Notify via WebSocket
            AuctionStatusMessage message = new AuctionStatusMessage();
            message.setAuctionId(auction.getId());
            message.setStatus(AuctionStatus.ACTIVE);
            message.setMessage("Auction has started!");

            messagingTemplate.convertAndSend("/topic/auction/" + auction.getId() + "/status", message);
        }
    }

    /**
     * Mark active auctions as ending soon (last 5 minutes)
     */
    private void markAuctionsEndingSoon(LocalDateTime now) {
        LocalDateTime fiveMinutesFromNow = now.plusMinutes(5);

        List<Auction> endingSoonAuctions = auctionRepository.findAuctionsEndingSoon(now, fiveMinutesFromNow);

        for (Auction auction : endingSoonAuctions) {
            if (auction.getStatus() == AuctionStatus.ACTIVE) {
                auction.setStatus(AuctionStatus.ENDING_SOON);
                auctionRepository.save(auction);

                System.out.println("Auction ending soon: " + auction.getId());

                // Notify via WebSocket
                AuctionStatusMessage message = new AuctionStatusMessage();
                message.setAuctionId(auction.getId());
                message.setStatus(AuctionStatus.ENDING_SOON);
                message.setMessage("Auction ending in less than 5 minutes!");

                messagingTemplate.convertAndSend("/topic/auction/" + auction.getId() + "/status", message);
            }
        }
    }

    /**
     * End auctions that have passed their end time
     */
    private void endExpiredAuctions(LocalDateTime now) {
        List<Auction> expiredAuctions = auctionRepository.findExpiredAuctions(now);

        for (Auction auction : expiredAuctions) {
            endAuction(auction);
        }
    }

    /**
     * End a single auction and declare winner
     */
    @Transactional
    public void endAuction(Auction auction) {
        auction.setStatus(AuctionStatus.ENDED);

        // Find winning bid
        Optional<Bid> winningBidOpt = bidRepository.findTopByAuctionIdOrderByAmountDesc(auction.getId());

        String winnerName = null;
        Long winnerId = null;

        if (winningBidOpt.isPresent()) {
            Bid winningBid = winningBidOpt.get();
            auction.setWinner(winningBid.getUser());
            winnerName = winningBid.getUser().getUsername();
            winnerId = winningBid.getUser().getId();

            System.out.println("Auction " + auction.getId() + " ended. Winner: " + winnerName +
                    " with bid: ₹" + auction.getCurrentPrice());
        } else {
            System.out.println("Auction " + auction.getId() + " ended with no bids");
        }

        auctionRepository.save(auction);

        // Notify via WebSocket
        AuctionStatusMessage message = new AuctionStatusMessage();
        message.setAuctionId(auction.getId());
        message.setStatus(AuctionStatus.ENDED);
        message.setWinnerName(winnerName);
        message.setWinnerId(winnerId);
        message.setFinalPrice(auction.getCurrentPrice());
        message.setMessage(winnerName != null ?
                "Auction ended! Winner: " + winnerName :
                "Auction ended with no bids");

        messagingTemplate.convertAndSend("/topic/auction/" + auction.getId() + "/status", message);
    }

    /**
     * Clean up old ended auctions (optional - run daily)
     */
    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    @Transactional
    public void cleanupOldAuctions() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // You can implement logic to archive or delete very old auctions
        System.out.println("Running daily auction cleanup task");

        // Example: Archive auctions older than 30 days
        // This is optional based on your requirements
    }
}