package com.shopsense.connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NormalizedOffer {

    private String platformName;
    private Long productVariantId;
    private String productVariantName;
    private BigDecimal originalPrice;
    private BigDecimal currentPrice;
    @Builder.Default
    private String currency = "INR";
    private String sellerName;
    private Double sellerRating;
    @Builder.Default
    private String availabilityStatus = "IN_STOCK";
    private String availabilityDetails;
    private String deliveryInfo;
    private String offerDetails;
    private String productUrl;
    @Builder.Default
    private LocalDateTime retrievedAt = LocalDateTime.now();
}
