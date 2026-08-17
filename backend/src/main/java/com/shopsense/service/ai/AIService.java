package com.shopsense.service.ai;

import com.shopsense.dto.ai.AIAnalysisResponse;

public interface AIService {

    /**
     * Generates product analysis and comparison insights for the specified
     * ProductVariant.
     *
     * @param variantId ID of the ProductVariant to analyze.
     * @return AIAnalysisResponse containing structured analysis results.
     */
    AIAnalysisResponse analyzeVariant(Long variantId);
}
