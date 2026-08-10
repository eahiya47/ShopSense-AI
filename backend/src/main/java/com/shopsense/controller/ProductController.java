package com.shopsense.controller;

import com.shopsense.dto.ProductDetailResponse;
import com.shopsense.dto.ProductSearchResponse;
import com.shopsense.dto.ProductVariantListResponse;
import com.shopsense.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Catalog", description = "Public endpoints for product search, catalog details, and variants")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Public deterministic database search for products matching query keyword or category with pagination.")
    public ResponseEntity<ProductSearchResponse> searchProducts(
            @RequestParam(name = "q", required = false, defaultValue = "") String query,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        ProductSearchResponse response = productService.searchProducts(query, category, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product details", description = "Public endpoint returning detailed catalog information and specifications for a product.")
    public ResponseEntity<ProductDetailResponse> getProductById(@PathVariable Long productId) {
        ProductDetailResponse response = productService.getProductById(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}/variants")
    @Operation(summary = "Get product variants", description = "Public endpoint returning available variants and selectable configuration attributes for a product.")
    public ResponseEntity<ProductVariantListResponse> getProductVariants(@PathVariable Long productId) {
        ProductVariantListResponse response = productService.getProductVariants(productId);
        return ResponseEntity.ok(response);
    }
}
