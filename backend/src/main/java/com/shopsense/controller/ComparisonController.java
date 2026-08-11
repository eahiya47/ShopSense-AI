package com.shopsense.controller;

import com.shopsense.dto.ProductComparisonResponse;
import com.shopsense.service.ComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
@Tag(name = "Product Comparison", description = "Public endpoints for comparing product variants across marketplace platforms")
public class ComparisonController {

    private final ComparisonService comparisonService;

    @GetMapping("/{variantId}/comparison")
    @Operation(summary = "Get product variant marketplace comparison", description = "Public endpoint returning live normalized marketplace comparison offers and platform statuses for a selected product variant.")
    public ResponseEntity<ProductComparisonResponse> getProductComparison(@PathVariable Long variantId) {
        ProductComparisonResponse response = comparisonService.getComparisonForVariant(variantId);
        return ResponseEntity.ok(response);
    }
}
