package com.shopsense.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductComparisonResponse {
    private ProductVariantResponse variant;
    private List<ComparisonOfferResponse> offers;
    private List<PlatformStatusResponse> platformStatus;
}
