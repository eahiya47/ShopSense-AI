package com.shopsense.service;

import com.shopsense.dto.PriceHistoryItemResponse;
import com.shopsense.dto.PriceHistoryResponse;
import com.shopsense.entity.Platform;
import com.shopsense.entity.PriceHistory;
import com.shopsense.entity.ProductVariant;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.PriceHistoryRepository;
import com.shopsense.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public void recordPriceSnapshotIfNeeded(
            ProductVariant variant,
            Platform platform,
            BigDecimal currentPrice,
            BigDecimal originalPrice,
            String currency) {

        if (variant == null || platform == null || currentPrice == null) {
            return;
        }

        Optional<PriceHistory> latestSnapshotOpt = priceHistoryRepository
                .findTopByProductVariantIdAndPlatformIdOrderByRecordedAtDesc(variant.getId(), platform.getId());

        if (latestSnapshotOpt.isPresent()) {
            PriceHistory latestSnapshot = latestSnapshotOpt.get();
            if (latestSnapshot.getPrice() != null && latestSnapshot.getPrice().compareTo(currentPrice) == 0) {
                log.debug("Price has not changed for variant {} on platform {}. Skipping duplicate snapshot.",
                        variant.getId(), platform.getName());
                return;
            }
        }

        PriceHistory newSnapshot = PriceHistory.builder()
                .productVariant(variant)
                .platform(platform)
                .price(currentPrice)
                .originalPrice(originalPrice)
                .currency(currency != null ? currency : "INR")
                .build();

        priceHistoryRepository.save(newSnapshot);
        log.info("Recorded price history snapshot for variant {} on platform {}: {}",
                variant.getId(), platform.getName(), currentPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceHistoryResponse getPriceHistoryForVariant(
            Long variantId,
            Long platformId,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("Product variant not found with id: " + variantId));

        List<PriceHistory> historyList;

        if (platformId != null && startDate != null && endDate != null) {
            historyList = priceHistoryRepository
                    .findByProductVariantIdAndPlatformIdAndRecordedAtBetweenOrderByRecordedAtAsc(
                            variantId, platformId, startDate, endDate);
        } else if (platformId != null) {
            historyList = priceHistoryRepository
                    .findByProductVariantIdAndPlatformIdOrderByRecordedAtAsc(variantId, platformId);
        } else if (startDate != null && endDate != null) {
            historyList = priceHistoryRepository
                    .findByProductVariantIdAndRecordedAtBetweenOrderByRecordedAtAsc(variantId, startDate, endDate);
        } else {
            historyList = priceHistoryRepository
                    .findByProductVariantIdOrderByRecordedAtAsc(variantId);
        }

        List<PriceHistoryItemResponse> historyItems = historyList.stream()
                .map(ph -> PriceHistoryItemResponse.builder()
                        .id(ph.getId())
                        .platformId(ph.getPlatform() != null ? ph.getPlatform().getId() : null)
                        .platformName(ph.getPlatform() != null ? ph.getPlatform().getName() : null)
                        .price(ph.getPrice())
                        .originalPrice(ph.getOriginalPrice())
                        .currency(ph.getCurrency())
                        .recordedAt(ph.getRecordedAt())
                        .build())
                .collect(Collectors.toList());

        return PriceHistoryResponse.builder()
                .variantId(variant.getId())
                .variantName(variant.getVariantName())
                .history(historyItems)
                .build();
    }
}
