package com.shopsense.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.client.gemini.GeminiClient;
import com.shopsense.dto.ProductComparisonResponse;
import com.shopsense.dto.VariantReviewsResponse;
import com.shopsense.dto.ai.AIAnalysisResponse;
import com.shopsense.entity.Category;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductSpecification;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.VariantAttribute;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.ProductSpecificationRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.VariantAttributeRepository;
import com.shopsense.service.ComparisonService;
import com.shopsense.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AIServiceTest {

        @Mock
        private ProductVariantRepository productVariantRepository;

        @Mock
        private ProductSpecificationRepository productSpecificationRepository;

        @Mock
        private VariantAttributeRepository variantAttributeRepository;

        @Mock
        private ComparisonService comparisonService;

        @Mock
        private ReviewService reviewService;

        @Mock
        private GeminiClient geminiClient;

        @Spy
        private ObjectMapper objectMapper = new ObjectMapper();

        @InjectMocks
        private GeminiAIService aiService;

        private Product sampleProduct;
        private ProductVariant sampleVariant;

        @BeforeEach
        void setUp() {
                Category category = Category.builder().id(10L).name("Smartphone").build();
                sampleProduct = Product.builder()
                                .id(102L)
                                .brand("Apple")
                                .model("16 Pro")
                                .category(category)
                                .description("Apple iPhone 16 Pro")
                                .build();

                sampleVariant = ProductVariant.builder()
                                .id(1002L)
                                .product(sampleProduct)
                                .variantName("256GB / Natural Titanium")
                                .isDefault(false)
                                .build();
        }

        @Test
        @DisplayName("Should successfully generate AI analysis for valid ProductVariant")
        void testAnalyzeVariant_Success() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                ProductSpecification spec = ProductSpecification.builder()
                                .attributeName("Processor")
                                .attributeValue("A18 Pro")
                                .build();
                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L))
                                .thenReturn(List.of(spec));

                VariantAttribute attr = VariantAttribute.builder()
                                .attributeName("Storage")
                                .attributeValue("256GB")
                                .build();
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of(attr));

                ProductComparisonResponse comparisonResponse = ProductComparisonResponse.builder()
                                .offers(List.of())
                                .build();
                when(comparisonService.getComparisonForVariant(1002L)).thenReturn(comparisonResponse);

                VariantReviewsResponse reviewsResponse = VariantReviewsResponse.builder()
                                .reviews(List.of())
                                .build();
                when(reviewService.getReviewsForVariant(1002L, null, 15)).thenReturn(reviewsResponse);

                String mockGeminiOutput = """
                                {
                                  "summary": "Excellent flagship smartphone.",
                                  "strengths": ["Fast A18 Pro chip", "Great display"],
                                  "drawbacks": ["Expensive"],
                                  "valueAssessment": "Good long term value.",
                                  "reviewInsights": "Users praise performance.",
                                  "bestOfferRecommendation": "Check Flipkart offer.",
                                  "buyingGuidance": "Buy if budget permits."
                                }
                                """;
                when(geminiClient.generateContent(anyString())).thenReturn(mockGeminiOutput);

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response).isNotNull();
                assertThat(response.getVariantId()).isEqualTo(1002L);
                assertThat(response.getProductName()).isEqualTo("Apple 16 Pro");
                assertThat(response.getVariantName()).isEqualTo("256GB / Natural Titanium");
                assertThat(response.getSummary()).isEqualTo("Excellent flagship smartphone.");
                assertThat(response.getStrengths()).containsExactly("Fast A18 Pro chip", "Great display");

                ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
                verify(geminiClient).generateContent(promptCaptor.capture());
                String prompt = promptCaptor.getValue();
                assertThat(prompt).contains("Apple 16 Pro");
                assertThat(prompt).contains("A18 Pro");
                assertThat(prompt).contains("256GB");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when variant does not exist")
        void testAnalyzeVariant_NotFound() {
                when(productVariantRepository.findById(9999L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> aiService.analyzeVariant(9999L));
                verifyNoInteractions(geminiClient);
        }

        @Test
        @DisplayName("Should return safe fallback response when Gemini client fails")
        void testAnalyzeVariant_GeminiFailureFallback() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                when(geminiClient.generateContent(anyString()))
                                .thenThrow(new RuntimeException("Gemini service connection error"));

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response).isNotNull();
                assertThat(response.getVariantId()).isEqualTo(1002L);
                assertThat(response.getSummary()).contains("temporarily unavailable");
                assertThat(response.getBestOfferRecommendation()).contains("consult the live marketplace comparison");
        }

        @Test
        @DisplayName("Should return safe fallback response when Gemini returns malformed output")
        void testAnalyzeVariant_MalformedGeminiOutputFallback() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                when(geminiClient.generateContent(anyString()))
                                .thenReturn("This is malformed non-JSON output from Gemini API");

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response).isNotNull();
                assertThat(response.getVariantId()).isEqualTo(1002L);
                assertThat(response.getSummary()).contains("temporarily unavailable");
                assertThat(response.getStrengths()).isEmpty();
                assertThat(response.getDrawbacks()).isEmpty();
                assertThat(response.getValueAssessment()).contains("unavailable");
                assertThat(response.getReviewInsights()).contains("unavailable");
        }
}
