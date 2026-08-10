package com.shopsense.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "platform_offers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_variant_platform", columnNames = { "product_variant_id", "platform_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "platform_id", nullable = false)
    private Platform platform;

    @Column(name = "original_price", precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @Column(name = "current_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "seller_name", length = 255)
    private String sellerName;

    @Column(name = "seller_rating", precision = 3, scale = 2)
    private BigDecimal sellerRating;

    @Column(name = "availability_status", nullable = false, length = 30)
    @Builder.Default
    private String availabilityStatus = "IN_STOCK";

    @Column(name = "availability_details", length = 500)
    private String availabilityDetails;

    @Column(name = "delivery_info", length = 500)
    private String deliveryInfo;

    @Column(name = "offer_details", columnDefinition = "TEXT")
    private String offerDetails;

    @Column(name = "product_url", nullable = false, length = 1000)
    private String productUrl;

    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;
}
