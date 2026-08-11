package com.shopsense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantReviewsResponse {
    private Long variantId;
    private PlatformResponse platform;
    private List<ReviewResponse> reviews;
    private Integer totalReviews;
    private LocalDateTime lastUpdated;
}
