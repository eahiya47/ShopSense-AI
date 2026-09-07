package com.shopsense.service;

import com.shopsense.dto.PriceHistoryResponse;
import com.shopsense.entity.Platform;
import com.shopsense.entity.PriceHistory;
import com.shopsense.entity.ProductVariant;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.PriceHistoryRepository;
import com.shopsense.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PriceHistoryServiceTest {

    @Mock
    private PriceHistoryRepository priceHistoryRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private PriceHistoryServiceImpl priceHistoryService;

    private ProductVariant sampleVariant;
    private Platform amazonPlatform;

    @BeforeEach
    void setUp() {
        sampleVariant = ProductVariant.builder()
                .id(101L)
                .variantName("256GB - Natural Titanium")
                .isDefault(true)
                .build();

        amazonPlatform = Platform.builder()
                .id(1L)
                .name("Amazon")
                .websiteUrl("https://www.amazon.in")
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("Should create initial price history snapshot when no prior history exists")
    void testRecordPriceSnapshot_InitialCreation() {
        when(priceHistoryRepository.findTopByProductVariantIdAndPlatformIdOrderByIdDesc(101L, 1L))
                .thenReturn(Optional.empty());

        priceHistoryService.recordPriceSnapshotIfNeeded(
                sampleVariant,
                amazonPlatform,
                BigDecimal.valueOf(127990),
                BigDecimal.valueOf(134900),
                "INR"
        );

        ArgumentCaptor<PriceHistory> captor = ArgumentCaptor.forClass(PriceHistory.class);
        verify(priceHistoryRepository, times(1)).save(captor.capture());

        PriceHistory saved = captor.getValue();
        assertThat(saved.getProductVariant()).isEqualTo(sampleVariant);
        assertThat(saved.getPlatform()).isEqualTo(amazonPlatform);
        assertThat(saved.getPrice()).isEqualTo(BigDecimal.valueOf(127990));
        assertThat(saved.getOriginalPrice()).isEqualTo(BigDecimal.valueOf(134900));
        assertThat(saved.getCurrency()).isEqualTo("INR");
    }

    @Test
    @DisplayName("Should create new price history snapshot when price has changed")
    void testRecordPriceSnapshot_PriceChanged() {
        PriceHistory previousSnapshot = PriceHistory.builder()
                .id(10L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .price(BigDecimal.valueOf(127990))
                .recordedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(priceHistoryRepository.findTopByProductVariantIdAndPlatformIdOrderByIdDesc(101L, 1L))
                .thenReturn(Optional.of(previousSnapshot));

        priceHistoryService.recordPriceSnapshotIfNeeded(
                sampleVariant,
                amazonPlatform,
                BigDecimal.valueOf(124990), // Price dropped
                BigDecimal.valueOf(134900),
                "INR"
        );

        ArgumentCaptor<PriceHistory> captor = ArgumentCaptor.forClass(PriceHistory.class);
        verify(priceHistoryRepository, times(1)).save(captor.capture());

        PriceHistory saved = captor.getValue();
        assertThat(saved.getPrice()).isEqualTo(BigDecimal.valueOf(124990));
    }

    @Test
    @DisplayName("Should NOT create duplicate price history snapshot when price remains unchanged")
    void testRecordPriceSnapshot_UnchangedPrice_NoDuplicate() {
        PriceHistory previousSnapshot = PriceHistory.builder()
                .id(10L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .price(BigDecimal.valueOf(127990))
                .recordedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(priceHistoryRepository.findTopByProductVariantIdAndPlatformIdOrderByIdDesc(101L, 1L))
                .thenReturn(Optional.of(previousSnapshot));

        priceHistoryService.recordPriceSnapshotIfNeeded(
                sampleVariant,
                amazonPlatform,
                BigDecimal.valueOf(127990), // Same price
                BigDecimal.valueOf(134900),
                "INR"
        );

        verify(priceHistoryRepository, never()).save(any(PriceHistory.class));
    }

    @Test
    @DisplayName("Should fetch chronological price history for a valid product variant")
    void testGetPriceHistoryForVariant_Success() {
        PriceHistory snapshot1 = PriceHistory.builder()
                .id(1L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .price(BigDecimal.valueOf(129990))
                .originalPrice(BigDecimal.valueOf(134900))
                .currency("INR")
                .recordedAt(LocalDateTime.now().minusDays(5))
                .build();

        PriceHistory snapshot2 = PriceHistory.builder()
                .id(2L)
                .productVariant(sampleVariant)
                .platform(amazonPlatform)
                .price(BigDecimal.valueOf(127990))
                .originalPrice(BigDecimal.valueOf(134900))
                .currency("INR")
                .recordedAt(LocalDateTime.now().minusDays(1))
                .build();

        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(sampleVariant));
        when(priceHistoryRepository.findByProductVariantIdOrderByRecordedAtAsc(101L))
                .thenReturn(List.of(snapshot1, snapshot2));

        PriceHistoryResponse response = priceHistoryService.getPriceHistoryForVariant(101L, null, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getVariantId()).isEqualTo(101L);
        assertThat(response.getVariantName()).isEqualTo("256GB - Natural Titanium");
        assertThat(response.getHistory()).hasSize(2);
        assertThat(response.getHistory().get(0).getPrice()).isEqualTo(BigDecimal.valueOf(129990));
        assertThat(response.getHistory().get(1).getPrice()).isEqualTo(BigDecimal.valueOf(127990));
        assertThat(response.getHistory().get(0).getPlatformName()).isEqualTo("Amazon");
    }

    @Test
    @DisplayName("Should handle empty price history safely without throwing exception")
    void testGetPriceHistoryForVariant_EmptyHistory() {
        when(productVariantRepository.findById(101L)).thenReturn(Optional.of(sampleVariant));
        when(priceHistoryRepository.findByProductVariantIdOrderByRecordedAtAsc(101L))
                .thenReturn(Collections.emptyList());

        PriceHistoryResponse response = priceHistoryService.getPriceHistoryForVariant(101L, null, null, null);

        assertThat(response).isNotNull();
        assertThat(response.getVariantId()).isEqualTo(101L);
        assertThat(response.getHistory()).isEmpty();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for missing variant ID")
    void testGetPriceHistoryForVariant_MissingVariant() {
        when(productVariantRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> priceHistoryService.getPriceHistoryForVariant(9999L, null, null, null));
    }
}
