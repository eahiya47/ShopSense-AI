package com.shopsense.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {

    private Long variantId;
    private String productName;
    private String variantName;
    private String summary;
    private List<String> strengths;
    private List<String> drawbacks;
    private String valueAssessment;
    private String reviewInsights;
    private String bestOfferRecommendation;
    private String buyingGuidance;
    private LocalDateTime generatedAt;
}
