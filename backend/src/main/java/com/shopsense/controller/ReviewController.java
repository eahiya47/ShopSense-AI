package com.shopsense.controller;

import com.shopsense.dto.VariantReviewsResponse;
import com.shopsense.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
@Tag(name = "Product Reviews", description = "Public endpoint for retrieving stored customer review samples for product variants")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{variantId}/reviews")
    @Operation(summary = "Get product variant reviews", description = "Public endpoint returning currently stored recent customer reviews for a selected product variant.")
    public ResponseEntity<VariantReviewsResponse> getVariantReviews(
            @PathVariable Long variantId,
            @RequestParam(name = "platformId", required = false) Long platformId,
            @RequestParam(name = "limit", required = false, defaultValue = "15") Integer limit) {
        VariantReviewsResponse response = reviewService.getReviewsForVariant(variantId, platformId, limit);
        return ResponseEntity.ok(response);
    }
}
