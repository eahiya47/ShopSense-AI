package com.shopsense.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.client.gemini.GeminiClient;
import com.shopsense.dto.ProductComparisonResponse;
import com.shopsense.dto.VariantReviewsResponse;
import com.shopsense.dto.ai.AIAnalysisResponse;
import com.shopsense.entity.AISummary;
import com.shopsense.entity.Category;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductSpecification;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.VariantAttribute;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.AISummaryRepository;
import com.shopsense.repository.PlatformOfferRepository;
import com.shopsense.repository.ProductSpecificationRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.ReviewRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

        @Mock
        private AISummaryRepository aiSummaryRepository;

        @Mock
        private PlatformOfferRepository platformOfferRepository;

        @Mock
        private ReviewRepository reviewRepository;

        @Spy
        private ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

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

                aiService.setTtlHours(24);
        }

        @Test
        @DisplayName("Cache miss: generates AI analysis and saves to cache")
        void testAnalyzeVariant_CacheMiss_Success() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.empty());

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
                assertThat(response.getSummary()).isEqualTo("Excellent flagship smartphone.");

                ArgumentCaptor<AISummary> summaryCaptor = ArgumentCaptor.forClass(AISummary.class);
                verify(aiSummaryRepository).save(summaryCaptor.capture());
                AISummary savedSummary = summaryCaptor.getValue();
                assertThat(savedSummary.getProductVariant()).isEqualTo(sampleVariant);
                assertThat(savedSummary.getSummary()).contains("Excellent flagship smartphone.");
                assertThat(savedSummary.getExpiresAt()).isAfter(savedSummary.getGeneratedAt());
        }

        @Test
        @DisplayName("3. Fresh offer + fresh review: valid unexpired cache is returned and Gemini is NOT invoked")
        void testAnalyzeVariant_FreshOfferAndReview_CacheHit() throws Exception {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                LocalDateTime generatedAt = LocalDateTime.now().minusHours(2);
                AIAnalysisResponse cachedDto = AIAnalysisResponse.builder()
                                .variantId(1002L)
                                .productName("Apple 16 Pro")
                                .variantName("256GB / Natural Titanium")
                                .summary("Cached AI analysis summary")
                                .generatedAt(generatedAt)
                                .build();

                String cachedJson = objectMapper.writeValueAsString(cachedDto);
                AISummary cachedEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary(cachedJson)
                                .generatedAt(generatedAt)
                                .expiresAt(LocalDateTime.now().plusHours(22))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(cachedEntity));
                when(platformOfferRepository.findLatestLastUpdatedAtByProductVariantId(1002L))
                                .thenReturn(Optional.of(generatedAt.minusHours(1)));
                when(reviewRepository.findLatestFetchedAtByProductVariantId(1002L))
                                .thenReturn(Optional.of(generatedAt.minusHours(1)));

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response).isNotNull();
                assertThat(response.getSummary()).isEqualTo("Cached AI analysis summary");
                verifyNoInteractions(geminiClient);
        }

        @Test
        @DisplayName("1. Offer freshness invalidation: PlatformOffer updated after generatedAt invalidates cache")
        void testAnalyzeVariant_OfferFreshnessInvalidation_InvokesGemini() throws Exception {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                LocalDateTime generatedAt = LocalDateTime.now().minusHours(4);
                AIAnalysisResponse cachedDto = AIAnalysisResponse.builder()
                                .variantId(1002L)
                                .summary("Old Cached Summary")
                                .generatedAt(generatedAt)
                                .build();

                AISummary cachedEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary(objectMapper.writeValueAsString(cachedDto))
                                .generatedAt(generatedAt)
                                .expiresAt(LocalDateTime.now().plusHours(20))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(cachedEntity));
                // Offer updated 1 hour ago (newer than generatedAt 4 hours ago)
                when(platformOfferRepository.findLatestLastUpdatedAtByProductVariantId(1002L))
                                .thenReturn(Optional.of(generatedAt.plusHours(3)));

                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                String mockFreshOutput = """
                                {
                                  "summary": "Fresh summary due to offer update",
                                  "strengths": [],
                                  "drawbacks": [],
                                  "valueAssessment": "Value",
                                  "reviewInsights": "Reviews",
                                  "bestOfferRecommendation": "Offer",
                                  "buyingGuidance": "Guidance"
                                }
                                """;
                when(geminiClient.generateContent(anyString())).thenReturn(mockFreshOutput);

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response.getSummary()).isEqualTo("Fresh summary due to offer update");
                verify(geminiClient).generateContent(anyString());
        }

        @Test
        @DisplayName("2. Review freshness invalidation: Review fetched after generatedAt invalidates cache")
        void testAnalyzeVariant_ReviewFreshnessInvalidation_InvokesGemini() throws Exception {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                LocalDateTime generatedAt = LocalDateTime.now().minusHours(4);
                AISummary cachedEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary("{\"summary\":\"Stale review summary\"}")
                                .generatedAt(generatedAt)
                                .expiresAt(LocalDateTime.now().plusHours(20))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(cachedEntity));
                when(platformOfferRepository.findLatestLastUpdatedAtByProductVariantId(1002L))
                                .thenReturn(Optional.of(generatedAt.minusHours(1)));
                // Review fetched 1 hour ago (newer than generatedAt 4 hours ago)
                when(reviewRepository.findLatestFetchedAtByProductVariantId(1002L))
                                .thenReturn(Optional.of(generatedAt.plusHours(3)));

                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                String mockFreshOutput = """
                                {
                                  "summary": "Fresh summary due to review update",
                                  "strengths": [],
                                  "drawbacks": [],
                                  "valueAssessment": "Value",
                                  "reviewInsights": "Reviews",
                                  "bestOfferRecommendation": "Offer",
                                  "buyingGuidance": "Guidance"
                                }
                                """;
                when(geminiClient.generateContent(anyString())).thenReturn(mockFreshOutput);

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response.getSummary()).isEqualTo("Fresh summary due to review update");
                verify(geminiClient).generateContent(anyString());
        }

        @Test
        @DisplayName("4. Offer freshness repository failure: bypasses cache and proceeds to fresh generation safely")
        void testAnalyzeVariant_OfferRepositoryFailure_BypassesCache() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                AISummary cachedEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary("{\"summary\":\"Cached summary\"}")
                                .generatedAt(LocalDateTime.now().minusHours(1))
                                .expiresAt(LocalDateTime.now().plusHours(23))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(cachedEntity));
                when(platformOfferRepository.findLatestLastUpdatedAtByProductVariantId(1002L))
                                .thenThrow(new RuntimeException("DB Offer Error"));

                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                String mockFreshOutput = """
                                {
                                  "summary": "Generated despite offer DB failure",
                                  "strengths": [],
                                  "drawbacks": [],
                                  "valueAssessment": "Value",
                                  "reviewInsights": "Reviews",
                                  "bestOfferRecommendation": "Offer",
                                  "buyingGuidance": "Guidance"
                                }
                                """;
                when(geminiClient.generateContent(anyString())).thenReturn(mockFreshOutput);

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response.getSummary()).isEqualTo("Generated despite offer DB failure");
        }

        @Test
        @DisplayName("5. Review freshness repository failure: bypasses cache and proceeds to fresh generation safely")
        void testAnalyzeVariant_ReviewRepositoryFailure_BypassesCache() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                AISummary cachedEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary("{\"summary\":\"Cached summary\"}")
                                .generatedAt(LocalDateTime.now().minusHours(1))
                                .expiresAt(LocalDateTime.now().plusHours(23))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(cachedEntity));
                when(platformOfferRepository.findLatestLastUpdatedAtByProductVariantId(1002L))
                                .thenReturn(Optional.empty());
                when(reviewRepository.findLatestFetchedAtByProductVariantId(1002L))
                                .thenThrow(new RuntimeException("DB Review Error"));

                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                String mockFreshOutput = """
                                {
                                  "summary": "Generated despite review DB failure",
                                  "strengths": [],
                                  "drawbacks": [],
                                  "valueAssessment": "Value",
                                  "reviewInsights": "Reviews",
                                  "bestOfferRecommendation": "Offer",
                                  "buyingGuidance": "Guidance"
                                }
                                """;
                when(geminiClient.generateContent(anyString())).thenReturn(mockFreshOutput);

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response.getSummary()).isEqualTo("Generated despite review DB failure");
        }

        @Test
        @DisplayName("6. Empty offer/review data: valid unexpired cache is returned when no offers or reviews exist")
        void testAnalyzeVariant_EmptyOffersAndReviews_ReturnsCache() throws Exception {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                AIAnalysisResponse cachedDto = AIAnalysisResponse.builder()
                                .variantId(1002L)
                                .summary("Cached summary with empty offers/reviews")
                                .generatedAt(LocalDateTime.now().minusHours(1))
                                .build();

                AISummary cachedEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary(objectMapper.writeValueAsString(cachedDto))
                                .generatedAt(LocalDateTime.now().minusHours(1))
                                .expiresAt(LocalDateTime.now().plusHours(23))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(cachedEntity));
                when(platformOfferRepository.findLatestLastUpdatedAtByProductVariantId(1002L))
                                .thenReturn(Optional.empty());
                when(reviewRepository.findLatestFetchedAtByProductVariantId(1002L)).thenReturn(Optional.empty());

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response.getSummary()).isEqualTo("Cached summary with empty offers/reviews");
                verifyNoInteractions(geminiClient);
        }

        @Test
        @DisplayName("7. Expiration still works independently: expired cache triggers fresh generation regardless of offer/review timestamps")
        void testAnalyzeVariant_ExpirationStillWorksIndependently() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                AISummary expiredEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary("{\"summary\":\"Expired summary\"}")
                                .generatedAt(LocalDateTime.now().minusHours(25))
                                .expiresAt(LocalDateTime.now().minusHours(1))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(expiredEntity));
                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                String mockFreshOutput = """
                                {
                                  "summary": "Fresh summary after expiration",
                                  "strengths": [],
                                  "drawbacks": [],
                                  "valueAssessment": "Value",
                                  "reviewInsights": "Reviews",
                                  "bestOfferRecommendation": "Offer",
                                  "buyingGuidance": "Guidance"
                                }
                                """;
                when(geminiClient.generateContent(anyString())).thenReturn(mockFreshOutput);

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response.getSummary()).isEqualTo("Fresh summary after expiration");
                verify(geminiClient).generateContent(anyString());
                verifyNoInteractions(platformOfferRepository);
                verifyNoInteractions(reviewRepository);
        }

        @Test
        @DisplayName("8. Stale cache + Gemini failure: safe fallback returned and existing AISummary remains unchanged")
        void testAnalyzeVariant_StaleCache_GeminiFailure_PreservesExistingCache() {
                when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));

                LocalDateTime generatedAt = LocalDateTime.now().minusHours(5);
                AISummary cachedEntity = AISummary.builder()
                                .id(1L)
                                .productVariant(sampleVariant)
                                .summary("{\"summary\":\"Existing cached summary\"}")
                                .generatedAt(generatedAt)
                                .expiresAt(LocalDateTime.now().plusHours(19))
                                .build();

                when(aiSummaryRepository.findByProductVariantId(1002L)).thenReturn(Optional.of(cachedEntity));
                when(platformOfferRepository.findLatestLastUpdatedAtByProductVariantId(1002L))
                                .thenReturn(Optional.of(generatedAt.plusHours(2)));

                when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(102L)).thenReturn(List.of());
                when(variantAttributeRepository.findByVariantId(1002L)).thenReturn(List.of());
                when(comparisonService.getComparisonForVariant(1002L))
                                .thenReturn(ProductComparisonResponse.builder().build());
                when(reviewService.getReviewsForVariant(1002L, null, 15))
                                .thenReturn(VariantReviewsResponse.builder().build());

                when(geminiClient.generateContent(anyString()))
                                .thenThrow(new RuntimeException("Gemini API connection error"));

                AIAnalysisResponse response = aiService.analyzeVariant(1002L);

                assertThat(response.getSummary()).contains("temporarily unavailable");
                verify(aiSummaryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when variant does not exist")
        void testAnalyzeVariant_NotFound() {
                when(productVariantRepository.findById(9999L)).thenReturn(Optional.empty());

                assertThrows(ResourceNotFoundException.class, () -> aiService.analyzeVariant(9999L));
                verifyNoInteractions(geminiClient);
        }
}
