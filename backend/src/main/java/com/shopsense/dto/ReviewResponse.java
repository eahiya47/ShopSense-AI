package com.shopsense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String reviewerName;
    private BigDecimal rating;
    private String reviewTitle;
    private String reviewText;
    private LocalDate reviewDate;
    private Boolean verifiedPurchase;
    private String sourceUrl;
    private LocalDateTime fetchedAt;
}
