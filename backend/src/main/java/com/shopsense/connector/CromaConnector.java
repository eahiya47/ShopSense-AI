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
public class CromaConnector implements MarketplaceConnector {

    public static final String PLATFORM_NAME = "Croma";

    private boolean simulateException = false;
    private boolean simulateReviewException = false;
    private String exceptionMessage = "Croma API network timeout";
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
            return ConnectorResult.unavailable(PLATFORM_NAME, "Croma marketplace service is temporarily unavailable");
        }

        if (simulatedStatus == ConnectorStatus.NO_OFFER) {
            return ConnectorResult.noOffer(PLATFORM_NAME, "No suitable offer available on Croma for this variant");
        }

        if (variant == null) {
            return ConnectorResult.unavailable(PLATFORM_NAME, "Product variant cannot be null");
        }

        Long variantId = variant.getId() != null ? variant.getId() : 1001L;
        String variantName = variant.getVariantName() != null ? variant.getVariantName() : "Standard";

        BigDecimal basePrice = BigDecimal.valueOf(50000 + (variantId % 1000) * 100);
        BigDecimal currentPrice = basePrice.add(BigDecimal.valueOf(14490));
        BigDecimal originalPrice = currentPrice.add(BigDecimal.valueOf(5500));

        NormalizedOffer offer = NormalizedOffer.builder()
                .platformName(PLATFORM_NAME)
                .productVariantId(variantId)
                .productVariantName(variantName)
                .currentPrice(currentPrice)
                .originalPrice(originalPrice)
                .currency("INR")
                .sellerName("Croma Retail")
                .sellerRating(4.5)
                .availabilityStatus("IN_STOCK")
                .availabilityDetails("Only 3 left in stock")
                .deliveryInfo("Express Delivery Today")
                .offerDetails("Flat ₹1000 Instant Discount on ICICI Bank Cards")
                .productUrl("https://www.croma.com/p/MOCK" + variantId)
                .retrievedAt(LocalDateTime.now())
                .build();

        return ConnectorResult.available(offer);
    }

    @Override
    public List<NormalizedReview> fetchReviews(ProductVariant variant) {
        if (simulateReviewException || simulateException) {
            throw new ConnectorException("Failed to fetch Croma reviews: " + exceptionMessage);
        }

        if (simulatedStatus == ConnectorStatus.UNAVAILABLE || simulatedStatus == ConnectorStatus.NO_OFFER) {
            return Collections.emptyList();
        }

        Long variantId = (variant != null && variant.getId() != null) ? variant.getId() : 1001L;
        int count = customReviewCount != null ? customReviewCount : 15;

        List<NormalizedReview> reviews = new ArrayList<>();
        LocalDate baseDate = LocalDate.now().minusDays(1);

        for (int i = 1; i <= count; i++) {
            double rating = 4.0 + (i % 5 == 0 ? 1.0 : 0.0);
            reviews.add(NormalizedReview.builder()
                    .platformName(PLATFORM_NAME)
                    .reviewerName("Croma Shopper " + i)
                    .rating(rating)
                    .reviewTitle("Croma Retail Review #" + i)
                    .reviewText("Review text for Croma variant " + variantId + ". Great product overall!")
                    .reviewDate(baseDate.minusDays(i * 4L))
                    .verifiedPurchase(true)
                    .sourceUrl("https://www.croma.com/reviews/crm-" + variantId + "-" + i)
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
        this.exceptionMessage = "Croma API network timeout";
        this.customReviewCount = null;
    }
}
