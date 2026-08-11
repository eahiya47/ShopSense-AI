package com.shopsense.service;

import com.shopsense.connector.*;
import com.shopsense.dto.ProductComparisonResponse;
import com.shopsense.entity.Platform;
import com.shopsense.entity.PlatformOffer;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductVariant;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.PlatformOfferRepository;
import com.shopsense.repository.PlatformRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.VariantAttributeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ComparisonServiceTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private VariantAttributeRepository variantAttributeRepository;

    @Mock
    private PlatformRepository platformRepository;

    @Mock
    private PlatformOfferRepository platformOfferRepository;

    @Mock
    private ConnectorManager connectorManager;

    @InjectMocks
    private ComparisonServiceImpl comparisonService;

    private Product sampleProduct;
    private ProductVariant sampleVariant;
    private Platform amazonPlatform;

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
    }

    @Test
    @DisplayName("getComparisonForVariant returns valid response across mock platforms")
    void testGetComparisonForVariant_Success() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(Collections.emptyList());
        when(platformRepository.findByIsActiveTrue()).thenReturn(List.of(amazonPlatform));

        NormalizedOffer normOffer = NormalizedOffer.builder()
                .platformName("Amazon")
                .productVariantId(1002L)
                .productVariantName("256GB / Natural Titanium")
                .currentPrice(BigDecimal.valueOf(114999))
                .originalPrice(BigDecimal.valueOf(119999))
                .currency("INR")
                .sellerName("Amazon Retail")
                .sellerRating(4.7)
                .availabilityStatus("IN_STOCK")
                .deliveryInfo("Delivery tomorrow")
                .productUrl("https://www.amazon.in/dp/mock")
                .build();

        ConnectorResult connectorResult = ConnectorResult.available(normOffer);
        when(connectorManager.fetchOffersForVariant(eq(sampleVariant), any())).thenReturn(List.of(connectorResult));

        when(platformRepository.findByNameIgnoreCase("Amazon")).thenReturn(Optional.of(amazonPlatform));
        when(platformOfferRepository.findByProductVariantIdAndPlatformId(1002L, 1L)).thenReturn(Optional.empty());

        PlatformOffer savedOffer = PlatformOffer.builder()
                .id(10L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .originalPrice(BigDecimal.valueOf(119999))
                .currentPrice(BigDecimal.valueOf(114999))
                .currency("INR")
                .sellerName("Amazon Retail")
                .sellerRating(BigDecimal.valueOf(4.7))
                .availabilityStatus("IN_STOCK")
                .deliveryInfo("Delivery tomorrow")
                .productUrl("https://www.amazon.in/dp/mock")
                .build();

        when(platformOfferRepository.save(any(PlatformOffer.class))).thenReturn(savedOffer);

        ProductComparisonResponse response = comparisonService.getComparisonForVariant(1002L);

        assertNotNull(response);
        assertNotNull(response.getVariant());
        assertEquals(1002L, response.getVariant().getId());
        assertEquals(100L, response.getVariant().getProductId());

        assertEquals(1, response.getOffers().size());
        assertEquals("Amazon", response.getOffers().get(0).getPlatform().getName());
        assertEquals(BigDecimal.valueOf(114999), response.getOffers().get(0).getCurrentPrice());

        assertEquals(1, response.getPlatformStatus().size());
        assertEquals("AVAILABLE", response.getPlatformStatus().get(0).getStatus());

        verify(platformOfferRepository, times(1)).save(any(PlatformOffer.class));
    }

    @Test
    @DisplayName("getComparisonForVariant updates existing PlatformOffer when record already exists")
    void testGetComparisonForVariant_UpdatesExistingPlatformOffer() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(Collections.emptyList());
        when(platformRepository.findByIsActiveTrue()).thenReturn(List.of(amazonPlatform));

        NormalizedOffer normOffer = NormalizedOffer.builder()
                .platformName("Amazon")
                .productVariantId(1002L)
                .currentPrice(BigDecimal.valueOf(112999))
                .originalPrice(BigDecimal.valueOf(119999))
                .currency("INR")
                .sellerName("Amazon Retail")
                .sellerRating(4.8)
                .availabilityStatus("IN_STOCK")
                .deliveryInfo("Delivery today")
                .productUrl("https://www.amazon.in/dp/mock")
                .build();

        when(connectorManager.fetchOffersForVariant(eq(sampleVariant), any()))
                .thenReturn(List.of(ConnectorResult.available(normOffer)));
        when(platformRepository.findByNameIgnoreCase("Amazon")).thenReturn(Optional.of(amazonPlatform));

        PlatformOffer existingOffer = PlatformOffer.builder()
                .id(10L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .currentPrice(BigDecimal.valueOf(114999))
                .originalPrice(BigDecimal.valueOf(119999))
                .currency("INR")
                .build();

        when(platformOfferRepository.findByProductVariantIdAndPlatformId(1002L, 1L))
                .thenReturn(Optional.of(existingOffer));
        when(platformOfferRepository.save(any(PlatformOffer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProductComparisonResponse response = comparisonService.getComparisonForVariant(1002L);

        assertNotNull(response);
        assertEquals(1, response.getOffers().size());
        assertEquals(BigDecimal.valueOf(112999), response.getOffers().get(0).getCurrentPrice());

        verify(platformOfferRepository).save(existingOffer);
        assertEquals(BigDecimal.valueOf(112999), existingOffer.getCurrentPrice());
    }

    @Test
    @DisplayName("getComparisonForVariant throws ResourceNotFoundException for missing variant ID")
    void testGetComparisonForVariant_MissingVariant() {
        when(productVariantRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> comparisonService.getComparisonForVariant(9999L));
    }

    @Test
    @DisplayName("getComparisonForVariant handles connector failure and NO_OFFER status while returning valid overall response")
    void testGetComparisonForVariant_ConnectorFailureAndNoOffer() {
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(Collections.emptyList());

        ConnectorResult amazonResult = ConnectorResult.available(NormalizedOffer.builder()
                .platformName("Amazon")
                .productVariantId(1002L)
                .currentPrice(BigDecimal.valueOf(114999))
                .currency("INR")
                .build());

        ConnectorResult flipkartResult = ConnectorResult.noOffer("Flipkart", "No seller offer available");
        ConnectorResult cromaResult = ConnectorResult.unavailable("Croma", "Connection timeout");

        when(connectorManager.fetchOffersForVariant(eq(sampleVariant), any()))
                .thenReturn(List.of(amazonResult, flipkartResult, cromaResult));

        when(platformRepository.findByNameIgnoreCase("Amazon")).thenReturn(Optional.of(amazonPlatform));
        when(platformOfferRepository.findByProductVariantIdAndPlatformId(1002L, 1L)).thenReturn(Optional.empty());
        when(platformOfferRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ProductComparisonResponse response = comparisonService.getComparisonForVariant(1002L);

        assertNotNull(response);
        assertEquals(1, response.getOffers().size());
        assertEquals("Amazon", response.getOffers().get(0).getPlatform().getName());

        assertEquals(3, response.getPlatformStatus().size());
        assertTrue(response.getPlatformStatus().stream()
                .anyMatch(s -> "Amazon".equalsIgnoreCase(s.getPlatform()) && "AVAILABLE".equals(s.getStatus())));
        assertTrue(response.getPlatformStatus().stream()
                .anyMatch(s -> "Flipkart".equalsIgnoreCase(s.getPlatform()) && "NO_OFFER".equals(s.getStatus())));
        assertTrue(response.getPlatformStatus().stream()
                .anyMatch(s -> "Croma".equalsIgnoreCase(s.getPlatform()) && "UNAVAILABLE".equals(s.getStatus())));
    }
}
