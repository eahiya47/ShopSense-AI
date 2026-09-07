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
public class PriceHistoryResponse {
    private Long variantId;
    private String variantName;
    private List<PriceHistoryItemResponse> history;
}
