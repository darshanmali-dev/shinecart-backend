package com.college.shinecart.service;

import com.college.shinecart.dto.*;
import com.college.shinecart.entity.*;
import com.college.shinecart.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Create a new auction (Admin only)
     */
    @Transactional
    public AuctionDTO createAuction(CreateAuctionRequest request, User admin) {
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found with ID: " + request.getProductId()));

        // Validate time range
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new RuntimeException("End time must be after start time");
        }

        // Validate minimum duration (at least 1 hour)
        long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (durationMinutes < 60) {
            throw new RuntimeException("Auction must be at least 1 hour long");
        }

        // Create auction
        Auction auction = new Auction();
        product.setAvailability("In Auction");
        auction.setProduct(product);
        auction.setStartingPrice(request.getStartingPrice());
        auction.setReservePrice(request.getReservePrice());
        auction.setCurrentPrice(request.getStartingPrice());
        auction.setBidIncrement(request.getBidIncrement());
        auction.setStartTime(request.getStartTime());
        auction.setEndTime(request.getEndTime());
        auction.setCreatedBy(admin);
        auction.setTotalBids(0);

        // Set initial status
        LocalDateTime now = LocalDateTime.now();
        if (request.getStartTime().isAfter(now)) {
            auction.setStatus(AuctionStatus.UPCOMING);
        } else if (request.getStartTime().isBefore(now) && request.getEndTime().isAfter(now)) {
            auction.setStatus(AuctionStatus.ACTIVE);
        } else {
            throw new RuntimeException("Invalid auction times");
        }

        Auction savedAuction = auctionRepository.save(auction);

        return convertToDTO(savedAuction);
    }

    /**
     * Get all active auctions
     */
    public List<AuctionDTO> getActiveAuctions() {
        return auctionRepository.findByStatusOrderByEndTimeAsc(AuctionStatus.ACTIVE)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all upcoming auctions
     */
    public List<AuctionDTO> getUpcomingAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.UPCOMING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all ended auctions
     */
    public List<AuctionDTO> getEndedAuctions() {
        return auctionRepository.findByStatus(AuctionStatus.ENDED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all auctions (any status)
     */
    public List<AuctionDTO> getAllAuctions() {
        return auctionRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get auction by ID
     */
    public AuctionDTO getAuctionById(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Auction not found with ID: " + id));
        return convertToDTO(auction);
    }

    /**
     * Get auctions by product
     */
    public List<AuctionDTO> getAuctionsByProduct(Long productId) {
        return auctionRepository.findByProductId(productId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get auctions created by admin
     */
    public List<AuctionDTO> getAuctionsByCreator(Long userId) {
        return auctionRepository.findByCreatedById(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get auctions won by user
     */
    public List<AuctionDTO> getAuctionsWonByUser(Long userId) {
        return auctionRepository.findByWinnerId(userId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Cancel an auction (Admin only)
     */
    @Transactional
    public AuctionDTO cancelAuction(Long auctionId, User admin) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Can only cancel if not ended
        if (auction.getStatus() == AuctionStatus.ENDED) {
            throw new RuntimeException("Cannot cancel ended auction");
        }

        auction.setStatus(AuctionStatus.CANCELLED);
        Auction saved = auctionRepository.save(auction);

        // Notify via WebSocket
        AuctionStatusMessage message = new AuctionStatusMessage();
        message.setAuctionId(auction.getId());
        message.setStatus(AuctionStatus.CANCELLED);
        message.setMessage("This auction has been cancelled by the administrator");

        messagingTemplate.convertAndSend("/topic/auction/" + auction.getId() + "/status", message);

        return convertToDTO(saved);
    }

    /**
     * Extend auction end time (Admin only)
     */
    @Transactional
    public AuctionDTO extendAuction(Long auctionId, int minutesToAdd, User admin) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        if (!auction.isActive()) {
            throw new RuntimeException("Can only extend active auctions");
        }

        auction.setEndTime(auction.getEndTime().plusMinutes(minutesToAdd));
        Auction saved = auctionRepository.save(auction);

        // Notify via WebSocket
        BidUpdateMessage message = new BidUpdateMessage();
        message.setAuctionId(auction.getId());
        message.setNewPrice(auction.getCurrentPrice());
        message.setTotalBids(auction.getTotalBids());
        message.setTimeRemaining(calculateTimeRemaining(auction));
        message.setTimeExtended(true);

        messagingTemplate.convertAndSend("/topic/auction/" + auction.getId(), message);

        return convertToDTO(saved);
    }

    /**
     * Get auction statistics (for admin dashboard)
     */
    public AuctionStatsDTO getAuctionStatistics() {
        AuctionStatsDTO stats = new AuctionStatsDTO();

        stats.setTotalAuctions(auctionRepository.count());
        stats.setActiveAuctions(auctionRepository.countActiveAuctions());
        stats.setTotalBids(bidRepository.getTotalBidsCount());
        stats.setTotalRevenue(auctionRepository.getTotalRevenue());

        return stats;
    }

    /**
     * Convert Auction entity to DTO
     */
    private AuctionDTO convertToDTO(Auction auction) {
        AuctionDTO dto = AuctionDTO.builder()
                .id(auction.getId())
                .product(convertProductToDTO(auction.getProduct()))
                .startingPrice(auction.getStartingPrice())
                .reservePrice(auction.getReservePrice())
                .currentPrice(auction.getCurrentPrice())
                .bidIncrement(auction.getBidIncrement())
                .startTime(auction.getStartTime())
                .endTime(auction.getEndTime())
                .status(auction.getStatus())
                .totalBids(auction.getTotalBids())
                .createdAt(auction.getCreatedAt())
                .timeRemaining(calculateTimeRemaining(auction))
                .build();

        // Set winner info if exists
        if (auction.getWinner() != null) {
            dto.setWinnerName(auction.getWinner().getUsername());
            dto.setWinnerId(auction.getWinner().getId());
        }

        // Set creator info
        if (auction.getCreatedBy() != null) {
            dto.setCreatedByName(auction.getCreatedBy().getUsername());
        }

        return dto;
    }
    /**
     * Calculate time remaining in seconds
     */
    private Long calculateTimeRemaining(Auction auction) {
        if (auction.getStatus() == AuctionStatus.ENDED ||
                auction.getStatus() == AuctionStatus.CANCELLED) {
            return 0L;
        }

        LocalDateTime now = LocalDateTime.now();

        if (auction.getStatus() == AuctionStatus.UPCOMING) {
            // Return time until start
            return Duration.between(now, auction.getStartTime()).getSeconds();
        }

        // Return time until end
        long seconds = Duration.between(now, auction.getEndTime()).getSeconds();
        return Math.max(0, seconds);
    }



    /**
     * Convert Product to ProductDTO (simplified)
     */
    /**
     * Convert Product to ProductDetailDTO
     */
    private ProductDetailDTO convertProductToDTO(Product product) {
        ProductDetailDTO dto = new ProductDetailDTO();

        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setCategory(product.getCategory());
        dto.setMetal(product.getMetal());
        dto.setStone(product.getStone());
        dto.setRating(product.getRating());
        dto.setReviews(product.getReviews());
        dto.setDiscount(product.getDiscount());
        dto.setBadge(product.getBadge());
        dto.setSku(product.getSku());
        dto.setAvailability(product.getAvailability());
        dto.setDescription(product.getDescription());

        // Convert comma-separated images to List
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            dto.setImages(List.of(product.getImages().split(",")));
        } else if (product.getImage() != null) {
            dto.setImages(List.of(product.getImage()));
        }

        // Convert comma-separated sizes to List
        if (product.getSizes() != null && !product.getSizes().isEmpty()) {
            dto.setSizes(List.of(product.getSizes().split(",")));
        }

        // Convert features JSON string to List
        if (product.getFeatures() != null && !product.getFeatures().isEmpty()) {
            try {
                // Assuming features is stored as JSON array string: ["feature1","feature2"]
                String featuresJson = product.getFeatures();
                featuresJson = featuresJson.replace("[", "").replace("]", "").replace("\"", "");
                dto.setFeatures(List.of(featuresJson.split(",")));
            } catch (Exception e) {
                dto.setFeatures(List.of());
            }
        }

        // Convert specifications JSON string to List<Specification>
        if (product.getSpecifications() != null && !product.getSpecifications().isEmpty()) {
            try {
                // Parse specifications (you might need Jackson ObjectMapper for complex JSON)
                // For now, keeping it simple - you can enhance this later
                dto.setSpecifications(List.of());
            } catch (Exception e) {
                dto.setSpecifications(List.of());
            }
        }

        return dto;
    }

    /**
     * Get auctions for admin with filters
     */
    public List<AuctionDTO> getAuctionsForAdmin(String status, String search) {
        List<Auction> auctions;

        if (status != null && !status.isEmpty()) {
            try {
                AuctionStatus auctionStatus = AuctionStatus.valueOf(status.toUpperCase());
                auctions = auctionRepository.findByStatus(auctionStatus);
            } catch (IllegalArgumentException e) {
                auctions = auctionRepository.findAll();
            }
        } else {
            auctions = auctionRepository.findAll();
        }

        // Apply search filter if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            auctions = auctions.stream()
                    .filter(auction ->
                            auction.getProduct().getName().toLowerCase().contains(searchLower) ||
                                    auction.getProduct().getCategory().toLowerCase().contains(searchLower) ||
                                    auction.getProduct().getMetal().toLowerCase().contains(searchLower)
                    )
                    .collect(Collectors.toList());
        }

        return auctions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update an auction (Admin only - only UPCOMING auctions can be edited)
     */
    @Transactional
    public AuctionDTO updateAuction(Long auctionId, UpdateAuctionRequest request, User admin) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Only UPCOMING auctions can be edited
        if (auction.getStatus() != AuctionStatus.UPCOMING) {
            throw new RuntimeException("Only upcoming auctions can be edited. Current status: " + auction.getStatus());
        }

        // Validate time range if provided
        if (request.getStartTime() != null && request.getEndTime() != null) {
            if (request.getEndTime().isBefore(request.getStartTime())) {
                throw new RuntimeException("End time must be after start time");
            }

            // Validate minimum duration (at least 1 hour)
            long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
            if (durationMinutes < 60) {
                throw new RuntimeException("Auction must be at least 1 hour long");
            }
        }

        // Update fields if provided
        if (request.getStartingPrice() != null) {
            auction.setStartingPrice(request.getStartingPrice());
            auction.setCurrentPrice(request.getStartingPrice());
        }

        if (request.getReservePrice() != null) {
            auction.setReservePrice(request.getReservePrice());
        }

        if (request.getBidIncrement() != null) {
            auction.setBidIncrement(request.getBidIncrement());
        }

        if (request.getStartTime() != null) {
            auction.setStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            auction.setEndTime(request.getEndTime());
        }

        Auction saved = auctionRepository.save(auction);
        return convertToDTO(saved);
    }

    /**
     * Delete an auction (Admin only - only UPCOMING or CANCELLED)
     */
    @Transactional
    public void deleteAuction(Long auctionId, User admin) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Can only delete UPCOMING or CANCELLED auctions
        if (auction.getStatus() != AuctionStatus.UPCOMING &&
                auction.getStatus() != AuctionStatus.CANCELLED) {
            throw new RuntimeException("Can only delete upcoming or cancelled auctions. Current status: " + auction.getStatus());
        }

        bidRepository.deleteByAuctionId(auctionId);


        auctionRepository.delete(auction);
    }

    /**
     * Manually end an auction (Admin only)
     */
    @Transactional
    public AuctionDTO endAuctionManually(Long auctionId, User admin) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new RuntimeException("Auction not found"));

        // Can only end ACTIVE or ENDING_SOON auctions
        if (!auction.isActive()) {
            throw new RuntimeException("Can only end active auctions. Current status: " + auction.getStatus());
        }

        // End the auction
        auction.setStatus(AuctionStatus.ENDED);

        // Find winning bid
        Optional<Bid> winningBidOpt = bidRepository.findTopByAuctionIdOrderByAmountDesc(auctionId);

        String winnerName = null;
        Long winnerId = null;

        if (winningBidOpt.isPresent()) {
            Bid winningBid = winningBidOpt.get();
            auction.setWinner(winningBid.getUser());
            winnerName = winningBid.getUser().getUsername();
            winnerId = winningBid.getUser().getId();
        }

        Auction saved = auctionRepository.save(auction);

        // Notify via WebSocket
        AuctionStatusMessage message = new AuctionStatusMessage();
        message.setAuctionId(auction.getId());
        message.setStatus(AuctionStatus.ENDED);
        message.setWinnerName(winnerName);
        message.setWinnerId(winnerId);
        message.setFinalPrice(auction.getCurrentPrice());
        message.setMessage(winnerName != null ?
                "Auction ended manually by admin. Winner: " + winnerName :
                "Auction ended manually by admin with no bids");

        messagingTemplate.convertAndSend("/topic/auction/" + auction.getId() + "/status", message);

        return convertToDTO(saved);
    }
}