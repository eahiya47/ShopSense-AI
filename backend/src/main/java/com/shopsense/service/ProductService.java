package com.shopsense.service;

import com.shopsense.dto.ProductDetailResponse;
import com.shopsense.dto.ProductSearchResponse;
import com.shopsense.dto.ProductVariantListResponse;

public interface ProductService {
    ProductSearchResponse searchProducts(String query, String category, int page, int size);

    ProductDetailResponse getProductById(Long productId);

    ProductVariantListResponse getProductVariants(Long productId);
}
