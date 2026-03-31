package com.college.shinecart.dto;

import com.college.shinecart.entity.AuctionStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDTO {

    private Long id;
    private ProductDetailDTO product;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentPrice;
    private BigDecimal bidIncrement;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private AuctionStatus status;
    private String winnerName;
    private Long winnerId;
    private Integer totalBids;
    private Long timeRemaining; // in seconds
    private String createdByName;
    private LocalDateTime createdAt;
}