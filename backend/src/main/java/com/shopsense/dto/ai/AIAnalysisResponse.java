package com.shopsense.dto.ai;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime generatedAt;
}
