package com.shopsense.service;

import com.shopsense.dto.VariantReviewsResponse;

public interface ReviewService {

    VariantReviewsResponse getReviewsForVariant(Long variantId, Long platformId, Integer limit);

    void refreshReviewsForVariant(Long variantId);

    void refreshReviewsForVariantAndPlatform(Long variantId, String platformName);
}
