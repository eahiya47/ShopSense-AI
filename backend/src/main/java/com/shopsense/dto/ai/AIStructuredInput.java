package com.shopsense.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIStructuredInput {

    private Long variantId;
    private String productBrand;
    private String productModel;
    private String categoryName;
    private String productDescription;
    private String variantName;

    private List<SpecificationItem> specifications;
    private List<AttributeItem> variantAttributes;
    private List<OfferItem> marketplaceOffers;
    private List<ReviewItem> reviews;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecificationItem {
        private String name;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttributeItem {
        private String name;
        private String value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OfferItem {
        private String platformName;
        private BigDecimal currentPrice;
        private BigDecimal originalPrice;
        private String currency;
        private String sellerName;
        private Double sellerRating;
        private String availabilityStatus;
        private String availabilityDetails;
        private String deliveryInfo;
        private String offerDetails;
        private BigDecimal discountPercentage;
        private Boolean isCheapest;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewItem {
        private String platformName;
        private BigDecimal rating;
        private String title;
        private String text;
        private Boolean verifiedPurchase;
        private String reviewDate;
    }
}
