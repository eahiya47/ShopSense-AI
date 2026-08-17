package com.shopsense.controller;

import com.shopsense.dto.ai.AIAnalysisResponse;
import com.shopsense.service.ai.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
@Tag(name = "AI Product Analysis", description = "Endpoints for AI-driven product comparison and review synthesis")
public class AIAnalysisController {

    private final AIService aiService;

    @GetMapping("/{variantId}/ai-analysis")
    @Operation(summary = "Get AI Product Analysis", description = "Generates AI analysis and comparison summary for the specified ProductVariant.")
    public ResponseEntity<AIAnalysisResponse> getAIAnalysis(@PathVariable Long variantId) {
        AIAnalysisResponse response = aiService.analyzeVariant(variantId);
        return ResponseEntity.ok(response);
    }
}
