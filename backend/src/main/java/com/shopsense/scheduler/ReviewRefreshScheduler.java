package com.shopsense.scheduler;

import com.shopsense.entity.ProductVariant;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewRefreshScheduler {

    private final ProductVariantRepository productVariantRepository;
    private final ReviewService reviewService;

    /**
     * Periodically triggers background review refreshes for all active product
     * variants.
     * Configured by default to run once every 7 days (every Sunday at 2:00 AM or
     * via cron expression).
     */
    @Scheduled(cron = "${shopsense.reviews.refresh-cron:0 0 2 * * SUN}")
    public void scheduledReviewRefresh() {
        log.info("Starting background review refresh scheduler...");
        try {
            List<ProductVariant> variants = productVariantRepository.findAll();
            if (variants.isEmpty()) {
                log.info("No product variants found to refresh reviews.");
                return;
            }

            int successCount = 0;
            int failureCount = 0;

            for (ProductVariant variant : variants) {
                if (variant.getId() == null) {
                    continue;
                }
                try {
                    reviewService.refreshReviewsForVariant(variant.getId());
                    successCount++;
                } catch (Exception e) {
                    failureCount++;
                    log.error(
                            "Background review refresh failed for variant ID {}: {}. Continuing with remaining variants.",
                            variant.getId(), e.getMessage());
                }
            }

            log.info("Background review refresh scheduler completed. Successfully refreshed: {}, Failures: {}",
                    successCount, failureCount);
        } catch (Exception e) {
            log.error("Unexpected failure during background review refresh execution: {}", e.getMessage(), e);
        }
    }
}
