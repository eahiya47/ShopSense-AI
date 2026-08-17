package com.shopsense.controller;

import com.shopsense.dto.ai.AIAnalysisResponse;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.security.CustomUserDetailsService;
import com.shopsense.security.JwtAuthenticationEntryPoint;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.service.ai.AIService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AIAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AIAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AIService aiService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/ai-analysis should return 200 OK with AI analysis")
    void testGetAIAnalysis_Success() throws Exception {
        AIAnalysisResponse response = AIAnalysisResponse.builder()
                .variantId(1002L)
                .productName("Apple 16 Pro")
                .variantName("256GB / Natural Titanium")
                .summary("Excellent flagship smartphone.")
                .strengths(List.of("Fast A18 Pro chip"))
                .drawbacks(List.of("Expensive"))
                .valueAssessment("Good long term value.")
                .reviewInsights("Users praise performance.")
                .bestOfferRecommendation("Check Flipkart offer.")
                .buyingGuidance("Buy if budget permits.")
                .generatedAt(LocalDateTime.now())
                .build();

        when(aiService.analyzeVariant(1002L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/variants/1002/ai-analysis")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value(1002))
                .andExpect(jsonPath("$.productName").value("Apple 16 Pro"))
                .andExpect(jsonPath("$.summary").value("Excellent flagship smartphone."))
                .andExpect(jsonPath("$.strengths[0]").value("Fast A18 Pro chip"));
    }

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/ai-analysis should return 404 Not Found when variant does not exist")
    void testGetAIAnalysis_NotFound() throws Exception {
        when(aiService.analyzeVariant(9999L))
                .thenThrow(new ResourceNotFoundException("Product variant not found with id: 9999"));

        mockMvc.perform(get("/api/v1/variants/9999/ai-analysis")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product variant not found with id: 9999"));
    }
}
