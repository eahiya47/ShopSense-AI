package com.shopsense.service;

import com.shopsense.dto.ProductComparisonResponse;

public interface ComparisonService {

    ProductComparisonResponse getComparisonForVariant(Long variantId);
}
