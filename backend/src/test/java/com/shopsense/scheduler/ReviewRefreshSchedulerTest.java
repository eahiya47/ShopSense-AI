package com.shopsense.scheduler;

import com.shopsense.entity.ProductVariant;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewRefreshSchedulerTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewRefreshScheduler reviewRefreshScheduler;

    private ProductVariant variant1;
    private ProductVariant variant2;

    @BeforeEach
    void setUp() {
        variant1 = ProductVariant.builder().id(1001L).variantName("Variant 1").build();
        variant2 = ProductVariant.builder().id(1002L).variantName("Variant 2").build();
    }

    @Test
    @DisplayName("scheduledReviewRefresh invokes ReviewService for all active product variants")
    void testScheduledReviewRefresh_ProcessesAllVariants() {
        when(productVariantRepository.findAll()).thenReturn(List.of(variant1, variant2));

        assertDoesNotThrow(() -> reviewRefreshScheduler.scheduledReviewRefresh());

        verify(reviewService, times(1)).refreshReviewsForVariant(1001L);
        verify(reviewService, times(1)).refreshReviewsForVariant(1002L);
    }

    @Test
    @DisplayName("Failure refreshing one variant does not prevent other variants from being refreshed")
    void testScheduledReviewRefresh_FailureIsolation() {
        when(productVariantRepository.findAll()).thenReturn(List.of(variant1, variant2));
        doThrow(new RuntimeException("Marketplace connection timeout"))
                .when(reviewService).refreshReviewsForVariant(1001L);

        assertDoesNotThrow(() -> reviewRefreshScheduler.scheduledReviewRefresh());

        verify(reviewService, times(1)).refreshReviewsForVariant(1001L);
        verify(reviewService, times(1)).refreshReviewsForVariant(1002L);
    }

    @Test
    @DisplayName("scheduledReviewRefresh handles empty variant list gracefully")
    void testScheduledReviewRefresh_EmptyVariants() {
        when(productVariantRepository.findAll()).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> reviewRefreshScheduler.scheduledReviewRefresh());

        verifyNoInteractions(reviewService);
    }
}
