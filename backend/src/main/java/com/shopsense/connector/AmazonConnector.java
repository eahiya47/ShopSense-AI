package com.shopsense.connector;

import com.shopsense.entity.ProductVariant;
import com.shopsense.exception.ConnectorException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class AmazonConnector implements MarketplaceConnector {

    public static final String PLATFORM_NAME = "Amazon";

    private boolean simulateException = false;
    private boolean simulateReviewException = false;
    private String exceptionMessage = "Amazon API connection timeout";
    private ConnectorStatus simulatedStatus = null;
    private Integer customReviewCount = null;

    @Override
    public String getPlatformName() {
        return PLATFORM_NAME;
    }

    @Override
    public ConnectorResult fetchOffer(ProductVariant variant) {
        if (simulateException) {
            throw new ConnectorException(exceptionMessage);
        }

        if (simulatedStatus == ConnectorStatus.UNAVAILABLE) {
            return ConnectorResult.unavailable(PLATFORM_NAME, "Amazon marketplace service is temporarily unavailable");
        }

        if (simulatedStatus == ConnectorStatus.NO_OFFER) {
            return ConnectorResult.noOffer(PLATFORM_NAME, "No suitable offer available on Amazon for this variant");
        }

        if (variant == null) {
            return ConnectorResult.unavailable(PLATFORM_NAME, "Product variant cannot be null");
        }

        Long variantId = variant.getId() != null ? variant.getId() : 1001L;
        String variantName = variant.getVariantName() != null ? variant.getVariantName() : "Standard";

        BigDecimal basePrice = BigDecimal.valueOf(50000 + (variantId % 1000) * 100);
        BigDecimal currentPrice = basePrice.add(BigDecimal.valueOf(14999));
        BigDecimal originalPrice = currentPrice.add(BigDecimal.valueOf(5000));

        NormalizedOffer offer = NormalizedOffer.builder()
                .platformName(PLATFORM_NAME)
                .productVariantId(variantId)
                .productVariantName(variantName)
                .currentPrice(currentPrice)
                .originalPrice(originalPrice)
                .currency("INR")
                .sellerName("Appario Retail Private Ltd")
                .sellerRating(4.7)
                .availabilityStatus("IN_STOCK")
                .availabilityDetails("In stock")
                .deliveryInfo("FREE Instant Delivery by Tomorrow")
                .offerDetails("10% instant discount up to ₹1500 on SBI Credit Cards")
                .productUrl("https://www.amazon.in/dp/B0MOCK" + variantId)
                .retrievedAt(LocalDateTime.now())
                .build();

        return ConnectorResult.available(offer);
    }

    @Override
    public List<NormalizedReview> fetchReviews(ProductVariant variant) {
        if (simulateReviewException || simulateException) {
            throw new ConnectorException("Failed to fetch Amazon reviews: " + exceptionMessage);
        }

        if (simulatedStatus == ConnectorStatus.UNAVAILABLE || simulatedStatus == ConnectorStatus.NO_OFFER) {
            return Collections.emptyList();
        }

        Long variantId = (variant != null && variant.getId() != null) ? variant.getId() : 1001L;
        int count = customReviewCount != null ? customReviewCount : 12;

        List<NormalizedReview> reviews = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().minusDays(1);

        for (int i = 1; i <= count; i++) {
            double rating = 4.0 + (i % 2 == 0 ? 1.0 : 0.5);
            reviews.add(NormalizedReview.builder()
                    .platformName(PLATFORM_NAME)
                    .reviewerName("Amazon Buyer " + i)
                    .rating(rating)
                    .reviewTitle("Great Amazon Purchase #" + i)
                    .reviewText("Verified review for variant " + variantId + ". Excellent quality and speedy delivery!")
                    .reviewDate(baseDate.minusDays(i * 2L))
                    .verifiedPurchase(true)
                    .sourceUrl("https://www.amazon.in/review/amz-" + variantId + "-" + i)
                    .fetchedAt(LocalDateTime.now())
                    .build());
        }

        return reviews;
    }

    public void setSimulateException(boolean simulateException) {
        this.simulateException = simulateException;
    }

    public void setSimulateException(boolean simulateException, String message) {
        this.simulateException = simulateException;
        this.exceptionMessage = message;
    }

    public void setSimulateReviewException(boolean simulateReviewException) {
        this.simulateReviewException = simulateReviewException;
    }

    public void setSimulatedStatus(ConnectorStatus simulatedStatus) {
        this.simulatedStatus = simulatedStatus;
    }

    public void setCustomReviewCount(Integer customReviewCount) {
        this.customReviewCount = customReviewCount;
    }

    public void reset() {
        this.simulateException = false;
        this.simulateReviewException = false;
        this.simulatedStatus = null;
        this.exceptionMessage = "Amazon API connection timeout";
        this.customReviewCount = null;
    }
}
