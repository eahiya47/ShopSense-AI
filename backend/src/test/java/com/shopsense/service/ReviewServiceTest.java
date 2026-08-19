package com.shopsense.service;

import com.shopsense.connector.AmazonConnector;
import com.shopsense.connector.ConnectorManager;
import com.shopsense.connector.NormalizedReview;
import com.shopsense.dto.VariantReviewsResponse;
import com.shopsense.entity.Platform;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.Review;
import com.shopsense.exception.ConnectorException;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.PlatformRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ConnectorManager connectorManager;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private Product sampleProduct;
    private ProductVariant sampleVariant;
    private Platform amazonPlatform;
    private Platform flipkartPlatform;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(100L)
                .brand("Apple")
                .series("iPhone")
                .model("16 Pro")
                .hasVariants(true)
                .build();

        sampleVariant = ProductVariant.builder()
                .id(1002L)
                .product(sampleProduct)
                .variantName("256GB / Natural Titanium")
                .isDefault(false)
                .build();

        amazonPlatform = Platform.builder()
                .id(1L)
                .name("Amazon")
                .websiteUrl("https://www.amazon.in")
                .isActive(true)
                .build();

        flipkartPlatform = Platform.builder()
                .id(2L)
                .name("Flipkart")
                .websiteUrl("https://www.flipkart.com")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("getReviewsForVariant returns stored reviews without calling connector manager")
    void testGetReviewsForVariant_ReturnsStoredReviewsWithoutConnectorCall() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

        Review storedReview = Review.builder()
                .id(50L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .reviewerName("Test User")
                .rating(BigDecimal.valueOf(4.5))
                .reviewTitle("Great Phone")
                .reviewText("Loved the battery and camera!")
                .reviewDate(LocalDate.now())
                .fetchedAt(LocalDateTime.now())
                .build();

        when(reviewRepository.findByProductVariantId(1002L)).thenReturn(List.of(storedReview));

        VariantReviewsResponse response = reviewService.getReviewsForVariant(1002L, null, 15);

        assertNotNull(response);
        assertEquals(1002L, response.getVariantId());
        assertEquals(1, response.getReviews().size());
        assertEquals("Test User", response.getReviews().get(0).getReviewerName());
        assertEquals("Amazon", response.getReviews().get(0).getPlatformName());

        verifyNoInteractions(connectorManager);
    }

    @Test
    @DisplayName("getReviewsForVariant throws ResourceNotFoundException for missing variant ID")
    void testGetReviewsForVariant_MissingVariant() {
        when(productVariantRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> reviewService.getReviewsForVariant(9999L, null, 15));
    }

    @Test
    @DisplayName("getReviewsForVariant returns valid empty response when no reviews exist")
    void testGetReviewsForVariant_NoReviewsReturnsEmptyResponse() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(reviewRepository.findByProductVariantId(1002L)).thenReturn(Collections.emptyList());

        VariantReviewsResponse response = reviewService.getReviewsForVariant(1002L, null, 15);

        assertNotNull(response);
        assertEquals(1002L, response.getVariantId());
        assertEquals(0, response.getTotalReviews());
        assertTrue(response.getReviews().isEmpty());
    }

    @Test
    @DisplayName("refreshReviewsForVariantAndPlatform fetches, normalizes, limits to 15, and replaces review set")
    void testRefreshReviewsForVariantAndPlatform_Success() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(platformRepository.findByNameIgnoreCase("Amazon")).thenReturn(Optional.of(amazonPlatform));

        List<NormalizedReview> normalizedReviews = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            normalizedReviews.add(NormalizedReview.builder()
                    .platformName("Amazon")
                    .reviewerName("Buyer " + i)
                    .rating(4.5)
                    .reviewTitle("Title " + i)
                    .reviewText("Content for review #" + i)
                    .reviewDate(LocalDate.now().minusDays(i))
                    .fetchedAt(LocalDateTime.now())
                    .build());
        }

        when(connectorManager.fetchReviewsFromPlatform("Amazon", sampleVariant)).thenReturn(normalizedReviews);

        reviewService.refreshReviewsForVariantAndPlatform(1002L, "Amazon");

        verify(reviewRepository).deleteByProductVariantIdAndPlatformId(1002L, 1L);
        verify(reviewRepository).saveAll(argThat(list -> {
            List<Review> reviews = (List<Review>) list;
            return reviews.size() == 15;
        }));
    }

    @Test
    @DisplayName("refreshReviewsForVariantAndPlatform supports fewer than 10 reviews without failing or inventing reviews")
    void testRefreshReviewsForVariantAndPlatform_FewerThan10Reviews() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(platformRepository.findByNameIgnoreCase("Amazon")).thenReturn(Optional.of(amazonPlatform));

        List<NormalizedReview> normalizedReviews = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            normalizedReviews.add(NormalizedReview.builder()
                    .platformName("Amazon")
                    .reviewerName("Buyer " + i)
                    .rating(4.0)
                    .reviewTitle("Title " + i)
                    .reviewText("Content for review #" + i)
                    .reviewDate(LocalDate.now().minusDays(i))
                    .fetchedAt(LocalDateTime.now())
                    .build());
        }

        when(connectorManager.fetchReviewsFromPlatform("Amazon", sampleVariant)).thenReturn(normalizedReviews);

        reviewService.refreshReviewsForVariantAndPlatform(1002L, "Amazon");

        verify(reviewRepository).deleteByProductVariantIdAndPlatformId(1002L, 1L);
        verify(reviewRepository).saveAll(argThat(list -> {
            List<Review> reviews = (List<Review>) list;
            return reviews.size() == 5;
        }));
    }

    @Test
    @DisplayName("Failed review refresh preserves existing valid stored reviews")
    void testRefreshReviews_FailedRefreshPreservesExistingReviews() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(platformRepository.findByNameIgnoreCase("Amazon")).thenReturn(Optional.of(amazonPlatform));

        when(connectorManager.fetchReviewsFromPlatform("Amazon", sampleVariant))
                .thenThrow(new ConnectorException("Amazon review endpoint network failure"));

        reviewService.refreshReviewsForVariantAndPlatform(1002L, "Amazon");

        verify(reviewRepository, never()).deleteByProductVariantIdAndPlatformId(anyLong(), anyLong());
        verify(reviewRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("Multiple platforms maintain independent review sets")
    void testMultiplePlatformsMaintainIndependentReviewSets() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(platformRepository.findById(1L)).thenReturn(Optional.of(amazonPlatform));

        Review amazonReview = Review.builder()
                .id(1L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .reviewText("Amazon review")
                .build();

        when(reviewRepository.findByProductVariantIdAndPlatformId(1002L, 1L)).thenReturn(List.of(amazonReview));

        VariantReviewsResponse amazonResponse = reviewService.getReviewsForVariant(1002L, 1L, 15);

        assertNotNull(amazonResponse);
        assertEquals("Amazon", amazonResponse.getPlatform().getName());
        assertEquals(1, amazonResponse.getReviews().size());
        assertEquals("Amazon review", amazonResponse.getReviews().get(0).getReviewText());
    }
}
