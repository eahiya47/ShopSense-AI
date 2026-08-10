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
public class ProductDetailResponse {
    private Long id;
    private String brand;
    private String series;
    private String model;
    private CategoryResponse category;
    private String description;
    private String imageUrl;
    private Boolean hasVariants;
    private List<ProductSpecificationResponse> specifications;
}
