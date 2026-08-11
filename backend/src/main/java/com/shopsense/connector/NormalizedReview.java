package com.shopsense.connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NormalizedReview {
    private String platformName;
    private String reviewerName;
    private Double rating;
    private String reviewTitle;
    private String reviewText;
    private LocalDate reviewDate;
    private Boolean verifiedPurchase;
    private String sourceUrl;
    private LocalDateTime fetchedAt;
}
