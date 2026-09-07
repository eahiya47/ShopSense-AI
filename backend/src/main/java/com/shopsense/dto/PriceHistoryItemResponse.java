package com.shopsense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryItemResponse {
    private Long id;
    private Long platformId;
    private String platformName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String currency;
    private LocalDateTime recordedAt;
}
