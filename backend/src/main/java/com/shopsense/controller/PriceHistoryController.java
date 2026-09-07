package com.shopsense.controller;

import com.shopsense.dto.PriceHistoryResponse;
import com.shopsense.service.PriceHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
@Tag(name = "Price History Management", description = "Endpoints for retrieving historical price data for product variants across platforms")
public class PriceHistoryController {

    private final PriceHistoryService priceHistoryService;

    @GetMapping("/{variantId}/price-history")
    @Operation(summary = "Get product variant price history", description = "Public endpoint returning chronological price history records for a selected product variant.")
    public ResponseEntity<PriceHistoryResponse> getPriceHistory(
            @PathVariable Long variantId,
            @RequestParam(required = false) Long platformId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        PriceHistoryResponse response = priceHistoryService.getPriceHistoryForVariant(variantId, platformId, startDate, endDate);
        return ResponseEntity.ok(response);
    }
}
