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
public class ProductSearchResponse {
    private String query;
    private Integer page;
    private Integer size;
    private Long totalResults;
    private Integer totalPages;
    private List<ProductSummaryResponse> products;
}
