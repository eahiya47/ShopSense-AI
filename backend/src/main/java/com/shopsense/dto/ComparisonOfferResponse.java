package com.shopsense.dto;

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
public class ComparisonOfferResponse {
    private PlatformResponse platform;
    private BigDecimal originalPrice;
    private BigDecimal currentPrice;
    private String currency;
    private String sellerName;
    private Double sellerRating;
    private String availabilityStatus;
    private String availabilityDetails;
    private String deliveryInfo;
    private String offerDetails;
    private String productUrl;
    private LocalDateTime lastUpdatedAt;
}
