package com.college.shinecart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BidDTO {
    private Long id;
    private Long auctionId;
    private String auctionTitle;
    private Long userId;
    private String userName;
    private BigDecimal amount;
    private LocalDateTime bidTime;
    private Boolean isWinning;
    private Boolean autoExtend;
}