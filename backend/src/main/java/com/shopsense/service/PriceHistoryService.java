package com.shopsense.service;

import com.shopsense.dto.PriceHistoryResponse;
import com.shopsense.entity.Platform;
import com.shopsense.entity.ProductVariant;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PriceHistoryService {

    void recordPriceSnapshotIfNeeded(
            ProductVariant variant,
            Platform platform,
            BigDecimal currentPrice,
            BigDecimal originalPrice,
            String currency
    );

    PriceHistoryResponse getPriceHistoryForVariant(
            Long variantId,
            Long platformId,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}
