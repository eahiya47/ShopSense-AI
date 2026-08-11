package com.shopsense.connector;

import com.shopsense.entity.ProductVariant;
import com.shopsense.exception.ConnectorException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CromaConnector implements MarketplaceConnector {

    public static final String PLATFORM_NAME = "Croma";

    private boolean simulateException = false;
    private String exceptionMessage = "Croma API network timeout";
    private ConnectorStatus simulatedStatus = null;

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

    public void setSimulateException(boolean simulateException) {
        this.simulateException = simulateException;
    }

    public void setSimulateException(boolean simulateException, String message) {
        this.simulateException = simulateException;
        this.exceptionMessage = message;
    }

    public void setSimulatedStatus(ConnectorStatus simulatedStatus) {
        this.simulatedStatus = simulatedStatus;
    }

    public void reset() {
        this.simulateException = false;
        this.simulatedStatus = null;
        this.exceptionMessage = "Croma API network timeout";
    }
}
