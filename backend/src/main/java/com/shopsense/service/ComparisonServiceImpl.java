package com.shopsense.service;

import com.shopsense.connector.ConnectorManager;
import com.shopsense.connector.ConnectorResult;
import com.shopsense.connector.ConnectorStatus;
import com.shopsense.connector.NormalizedOffer;
import com.shopsense.dto.*;
import com.shopsense.entity.Platform;
import com.shopsense.entity.PlatformOffer;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.VariantAttribute;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.PlatformOfferRepository;
import com.shopsense.repository.PlatformRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.VariantAttributeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ComparisonServiceImpl implements ComparisonService {

    private final ProductVariantRepository productVariantRepository;
    private final VariantAttributeRepository variantAttributeRepository;
    private final PlatformRepository platformRepository;
    private final PlatformOfferRepository platformOfferRepository;
    private final ConnectorManager connectorManager;

    @Override
    @Transactional
    public ProductComparisonResponse getComparisonForVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        ProductVariantResponse variantResponse = mapToVariantResponse(variant);

        List<Platform> activePlatforms = platformRepository.findByIsActiveTrue();
        List<String> activePlatformNames = !activePlatforms.isEmpty()
                ? activePlatforms.stream().map(Platform::getName).collect(Collectors.toList())
                : null;

        List<ConnectorResult> connectorResults = connectorManager.fetchOffersForVariant(variant, activePlatformNames);

        List<ComparisonOfferResponse> offers = new ArrayList<>();
        List<PlatformStatusResponse> platformStatuses = new ArrayList<>();

        for (ConnectorResult result : connectorResults) {
            String platformName = result.getPlatformName();
            ConnectorStatus status = result.getStatus();

            String statusMessage = (status == ConnectorStatus.NO_OFFER)
                    ? (result.getMessage() != null ? result.getMessage() : "No offer available on " + platformName)
                    : (status == ConnectorStatus.UNAVAILABLE)
                            ? (result.getErrorMessage() != null ? result.getErrorMessage()
                                    : "Marketplace data is temporarily unavailable.")
                            : null;

            platformStatuses.add(PlatformStatusResponse.builder()
                    .platform(platformName)
                    .status(status.name())
                    .message(statusMessage)
                    .build());

            if (status == ConnectorStatus.AVAILABLE && result.getOffer() != null) {
                NormalizedOffer normOffer = result.getOffer();

                Platform platform = platformRepository.findByNameIgnoreCase(platformName)
                        .orElseGet(() -> platformRepository.save(Platform.builder()
                                .name(platformName)
                                .websiteUrl("https://www." + platformName.toLowerCase() + ".com")
                                .isActive(true)
                                .build()));

                Optional<PlatformOffer> existingOfferOpt = platformOfferRepository
                        .findByProductVariantIdAndPlatformId(variant.getId(), platform.getId());

                PlatformOffer platformOffer;
                if (existingOfferOpt.isPresent()) {
                    platformOffer = existingOfferOpt.get();
                    platformOffer.setOriginalPrice(normOffer.getOriginalPrice());
                    platformOffer.setCurrentPrice(normOffer.getCurrentPrice());
                    platformOffer.setCurrency(normOffer.getCurrency() != null ? normOffer.getCurrency() : "INR");
                    platformOffer.setSellerName(normOffer.getSellerName());
                    platformOffer.setSellerRating(
                            normOffer.getSellerRating() != null ? BigDecimal.valueOf(normOffer.getSellerRating())
                                    : null);
                    platformOffer.setAvailabilityStatus(
                            normOffer.getAvailabilityStatus() != null ? normOffer.getAvailabilityStatus() : "IN_STOCK");
                    platformOffer.setAvailabilityDetails(normOffer.getAvailabilityDetails());
                    platformOffer.setDeliveryInfo(normOffer.getDeliveryInfo());
                    platformOffer.setOfferDetails(normOffer.getOfferDetails());
                    platformOffer.setProductUrl(normOffer.getProductUrl());
                } else {
                    platformOffer = PlatformOffer.builder()
                            .productVariant(variant)
                            .platform(platform)
                            .originalPrice(normOffer.getOriginalPrice())
                            .currentPrice(normOffer.getCurrentPrice())
                            .currency(normOffer.getCurrency() != null ? normOffer.getCurrency() : "INR")
                            .sellerName(normOffer.getSellerName())
                            .sellerRating(normOffer.getSellerRating() != null
                                    ? BigDecimal.valueOf(normOffer.getSellerRating())
                                    : null)
                            .availabilityStatus(
                                    normOffer.getAvailabilityStatus() != null ? normOffer.getAvailabilityStatus()
                                            : "IN_STOCK")
                            .availabilityDetails(normOffer.getAvailabilityDetails())
                            .deliveryInfo(normOffer.getDeliveryInfo())
                            .offerDetails(normOffer.getOfferDetails())
                            .productUrl(normOffer.getProductUrl())
                            .build();
                }

                PlatformOffer savedOffer = platformOfferRepository.save(platformOffer);

                PlatformResponse platformResponse = PlatformResponse.builder()
                        .id(platform.getId())
                        .name(platform.getName())
                        .websiteUrl(platform.getWebsiteUrl())
                        .logoUrl(platform.getLogoUrl())
                        .build();

                ComparisonOfferResponse offerResponse = ComparisonOfferResponse.builder()
                        .platform(platformResponse)
                        .originalPrice(savedOffer.getOriginalPrice())
                        .currentPrice(savedOffer.getCurrentPrice())
                        .currency(savedOffer.getCurrency())
                        .sellerName(savedOffer.getSellerName())
                        .sellerRating(savedOffer.getSellerRating() != null ? savedOffer.getSellerRating().doubleValue()
                                : null)
                        .availabilityStatus(savedOffer.getAvailabilityStatus())
                        .availabilityDetails(savedOffer.getAvailabilityDetails())
                        .deliveryInfo(savedOffer.getDeliveryInfo())
                        .offerDetails(savedOffer.getOfferDetails())
                        .productUrl(savedOffer.getProductUrl())
                        .lastUpdatedAt(savedOffer.getLastUpdatedAt())
                        .build();

                offers.add(offerResponse);
            }
        }

        return ProductComparisonResponse.builder()
                .variant(variantResponse)
                .offers(offers)
                .platformStatus(platformStatuses)
                .build();
    }

    private ProductVariantResponse mapToVariantResponse(ProductVariant variant) {
        List<VariantAttribute> attrs = variantAttributeRepository.findByVariantId(variant.getId());
        List<VariantAttributeResponse> attrResponses = attrs.stream()
                .map(a -> VariantAttributeResponse.builder()
                        .name(a.getAttributeName())
                        .value(a.getAttributeValue())
                        .build())
                .collect(Collectors.toList());

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .productId(variant.getProduct() != null ? variant.getProduct().getId() : null)
                .name(variant.getVariantName())
                .isDefault(variant.getIsDefault())
                .attributes(attrResponses)
                .build();
    }
}
