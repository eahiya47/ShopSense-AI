package com.shopsense.service;

import com.shopsense.connector.ConnectorManager;
import com.shopsense.connector.NormalizedReview;
import com.shopsense.dto.PlatformResponse;
import com.shopsense.dto.ReviewResponse;
import com.shopsense.dto.VariantReviewsResponse;
import com.shopsense.entity.Platform;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.Review;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.PlatformRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    public static final int TARGET_REVIEW_LIMIT = 15;

    private final ProductVariantRepository productVariantRepository;
    private final PlatformRepository platformRepository;
    private final ReviewRepository reviewRepository;
    private final ConnectorManager connectorManager;

    @Override
    @Transactional(readOnly = true)
    public VariantReviewsResponse getReviewsForVariant(Long variantId, Long platformId, Integer limit) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        PlatformResponse platformResponse = null;
        List<Review> storedReviews;

        if (platformId != null) {
            Platform platform = platformRepository.findById(platformId)
                    .orElseThrow(() -> new ResourceNotFoundException("Platform not found with id: " + platformId));

            platformResponse = PlatformResponse.builder()
                    .id(platform.getId())
                    .name(platform.getName())
                    .websiteUrl(platform.getWebsiteUrl())
                    .logoUrl(platform.getLogoUrl())
                    .build();

            storedReviews = reviewRepository.findByProductVariantIdAndPlatformId(variantId, platformId);
        } else {
            storedReviews = reviewRepository.findByProductVariantId(variantId);
        }

        int maxLimit = (limit != null && limit > 0) ? limit : TARGET_REVIEW_LIMIT;

        List<ReviewResponse> reviewResponses = storedReviews.stream()
                .sorted(Comparator.comparing(Review::getFetchedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(maxLimit)
                .map(this::mapToReviewResponse)
                .collect(Collectors.toList());

        LocalDateTime lastUpdated = storedReviews.stream()
                .map(Review::getFetchedAt)
                .filter(dt -> dt != null)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return VariantReviewsResponse.builder()
                .variantId(variant.getId())
                .platform(platformResponse)
                .reviews(reviewResponses)
                .totalReviews(reviewResponses.size())
                .lastUpdated(lastUpdated)
                .build();
    }

    @Override
    @Transactional
    public void refreshReviewsForVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        List<Platform> activePlatforms = platformRepository.findByIsActiveTrue();
        if (activePlatforms.isEmpty()) {
            connectorManager.getAvailableConnectors().forEach(connector -> {
                refreshReviewsForVariantAndPlatform(variant.getId(), connector.getPlatformName());
            });
        } else {
            for (Platform platform : activePlatforms) {
                refreshReviewsForVariantAndPlatform(variant.getId(), platform.getName());
            }
        }
    }

    @Override
    @Transactional
    public void refreshReviewsForVariantAndPlatform(Long variantId, String platformName) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        Platform platform = platformRepository.findByNameIgnoreCase(platformName)
                .orElseGet(() -> platformRepository.save(Platform.builder()
                        .name(platformName)
                        .websiteUrl("https://www." + platformName.toLowerCase() + ".com")
                        .isActive(true)
                        .build()));

        List<NormalizedReview> fetchedReviews;
        try {
            fetchedReviews = connectorManager.fetchReviewsFromPlatform(platformName, variant);
        } catch (Exception e) {
            log.warn(
                    "Marketplace review refresh failed for platform {} and variant {}: {}. Preserving existing reviews.",
                    platformName, variantId, e.getMessage());
            return; // Preserve existing reviews on refresh failure
        }

        if (fetchedReviews == null || fetchedReviews.isEmpty()) {
            log.info("No reviews returned for platform {} and variant {}. Preserving existing reviews.", platformName,
                    variantId);
            return; // Preserve existing valid review data
        }

        // Select top 10-15 recent relevant reviews
        List<NormalizedReview> selectedReviews = fetchedReviews.stream()
                .filter(r -> r.getReviewText() != null && !r.getReviewText().trim().isEmpty())
                .sorted(Comparator.comparing(NormalizedReview::getReviewDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(TARGET_REVIEW_LIMIT)
                .collect(Collectors.toList());

        if (selectedReviews.isEmpty()) {
            return;
        }

        // Delete existing temporary review set for this ProductVariant + Platform
        reviewRepository.deleteByProductVariantIdAndPlatformId(variant.getId(), platform.getId());

        // Save refreshed review set
        List<Review> newReviews = new ArrayList<>();
        for (NormalizedReview norm : selectedReviews) {
            Review review = Review.builder()
                    .productVariant(variant)
                    .platform(platform)
                    .reviewerName(norm.getReviewerName())
                    .rating(norm.getRating() != null ? BigDecimal.valueOf(norm.getRating()) : null)
                    .reviewTitle(norm.getReviewTitle())
                    .reviewText(norm.getReviewText())
                    .reviewDate(norm.getReviewDate())
                    .verifiedPurchase(norm.getVerifiedPurchase())
                    .sourceUrl(norm.getSourceUrl())
                    .fetchedAt(norm.getFetchedAt() != null ? norm.getFetchedAt() : LocalDateTime.now())
                    .build();
            newReviews.add(review);
        }

        reviewRepository.saveAll(newReviews);
        log.info("Successfully refreshed {} reviews for platform {} and variant {}", newReviews.size(), platformName,
                variantId);
    }

    private ReviewResponse mapToReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .reviewerName(review.getReviewerName())
                .rating(review.getRating())
                .reviewTitle(review.getReviewTitle())
                .reviewText(review.getReviewText())
                .reviewDate(review.getReviewDate())
                .verifiedPurchase(review.getVerifiedPurchase())
                .sourceUrl(review.getSourceUrl())
                .fetchedAt(review.getFetchedAt())
                .build();
    }
}
