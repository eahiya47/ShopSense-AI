package com.shopsense.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.client.gemini.GeminiClient;
import com.shopsense.dto.ComparisonOfferResponse;
import com.shopsense.dto.ProductComparisonResponse;
import com.shopsense.dto.ReviewResponse;
import com.shopsense.dto.VariantReviewsResponse;
import com.shopsense.dto.ai.AIAnalysisResponse;
import com.shopsense.dto.ai.AIStructuredInput;
import com.shopsense.entity.AISummary;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductVariant;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.AISummaryRepository;
import com.shopsense.repository.ProductSpecificationRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.VariantAttributeRepository;
import com.shopsense.service.ComparisonService;
import com.shopsense.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import com.shopsense.repository.PlatformOfferRepository;
import com.shopsense.repository.ReviewRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiAIService implements AIService {

        private final ProductVariantRepository productVariantRepository;
        private final ProductSpecificationRepository productSpecificationRepository;
        private final VariantAttributeRepository variantAttributeRepository;
        private final ComparisonService comparisonService;
        private final ReviewService reviewService;
        private final GeminiClient geminiClient;
        private final ObjectMapper objectMapper;
        private final AISummaryRepository aiSummaryRepository;
        private final PlatformOfferRepository platformOfferRepository;
        private final ReviewRepository reviewRepository;

        @Value("${shopsense.ai.cache.ttl-hours:24}")
        @Setter
        private long ttlHours = 24;

        @Override
        public AIAnalysisResponse analyzeVariant(Long variantId) {
                ProductVariant variant = productVariantRepository.findById(variantId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Product variant not found with id: " + variantId));

                Product product = variant.getProduct();
                String productName = product.getBrand() + " " + product.getModel();

                // 1. Check AI summary cache and data freshness
                try {
                        Optional<AISummary> cachedOpt = aiSummaryRepository.findByProductVariantId(variantId);
                        if (cachedOpt.isPresent()) {
                                AISummary cached = cachedOpt.get();
                                if (isCacheFresh(cached, variantId)) {
                                        try {
                                                AIAnalysisResponse response = objectMapper.readValue(
                                                                cached.getSummary(),
                                                                AIAnalysisResponse.class);
                                                log.info("AI Analysis cache hit for variant id: {}", variantId);
                                                return response;
                                        } catch (Exception parseEx) {
                                                log.warn("Failed to parse cached AI summary JSON for variant id: {}. Regenerating.",
                                                                variantId);
                                        }
                                }
                        }
                } catch (Exception e) {
                        log.error("AI Analysis cache lookup failed for variant id: {}. Continuing with fresh generation. Cause: {}",
                                        variantId, e.getMessage());
                }

                // 2. Cache miss, expired, or read failure -> gather fresh facts and invoke
                // Gemini
                AIStructuredInput input = buildStructuredInput(product, variant);

                try {
                        String prompt = buildPrompt(input);
                        String rawResponse = geminiClient.generateContent(prompt);
                        AIAnalysisResponse response = parseAndValidateResponse(rawResponse, variant.getId(),
                                        productName,
                                        variant.getVariantName());

                        // Save to cache only if successful and not fallback
                        if (!isFallbackResponse(response)) {
                                try {
                                        saveToCache(variant, response);
                                } catch (Exception cacheSaveEx) {
                                        log.error("Failed to save AI analysis to cache for variant id: {}. Cause: {}",
                                                        variantId, cacheSaveEx.getMessage());
                                }
                        }

                        return response;
                } catch (Exception e) {
                        log.error("AI Analysis failed for variant id: {}. Returning safe fallback. Cause: {}",
                                        variantId, e.getMessage());
                        return buildFallbackResponse(variant.getId(), productName, variant.getVariantName());
                }
        }

        private boolean isFallbackResponse(AIAnalysisResponse response) {
                return response != null && "AI product analysis is temporarily unavailable for this variant."
                                .equals(response.getSummary());
        }

        private boolean isCacheFresh(AISummary cached, Long variantId) {
                // 1. TTL Expiration Check
                if (!LocalDateTime.now().isBefore(cached.getExpiresAt())) {
                        log.info("AI Analysis cache expired for variant id: {}. Regenerating.", variantId);
                        return false;
                }

                // Invalidate legacy generic mock summary if present in cache
                if (cached.getSummary() != null && cached.getSummary()
                                .contains("This variant offers an excellent balance of high performance")) {
                        log.info("AI Analysis cache contained legacy generic mock summary for variant id: {}. Invalidating.",
                                        variantId);
                        return false;
                }

                LocalDateTime generatedAt = cached.getGeneratedAt();

                // 2. Marketplace Offer Freshness Check
                try {
                        Optional<LocalDateTime> latestOfferOpt = platformOfferRepository
                                        .findLatestLastUpdatedAtByProductVariantId(variantId);
                        if (latestOfferOpt.isPresent() && latestOfferOpt.get().isAfter(generatedAt)) {
                                log.info("AI Analysis cache stale for variant id: {} due to updated PlatformOffer. Regenerating.",
                                                variantId);
                                return false;
                        }
                } catch (Exception e) {
                        log.error("Failed to check PlatformOffer freshness for variant id: {}. Bypassing cache. Cause: {}",
                                        variantId, e.getMessage());
                        return false;
                }

                // 3. Review Freshness Check
                try {
                        Optional<LocalDateTime> latestReviewOpt = reviewRepository
                                        .findLatestFetchedAtByProductVariantId(variantId);
                        if (latestReviewOpt.isPresent() && latestReviewOpt.get().isAfter(generatedAt)) {
                                log.info("AI Analysis cache stale for variant id: {} due to updated Review sample. Regenerating.",
                                                variantId);
                                return false;
                        }
                } catch (Exception e) {
                        log.error("Failed to check Review freshness for variant id: {}. Bypassing cache. Cause: {}",
                                        variantId, e.getMessage());
                        return false;
                }

                return true;
        }

        private void saveToCache(ProductVariant variant, AIAnalysisResponse response) throws Exception {
                String jsonPayload = objectMapper.writeValueAsString(response);
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime expiresAt = now.plusHours(ttlHours);

                AISummary summary = aiSummaryRepository.findByProductVariantId(variant.getId())
                                .orElseGet(() -> AISummary.builder().productVariant(variant).build());

                summary.setSummary(jsonPayload);
                summary.setGeneratedAt(now);
                summary.setExpiresAt(expiresAt);

                aiSummaryRepository.save(summary);
        }

        private AIStructuredInput buildStructuredInput(Product product, ProductVariant variant) {
                // Specifications
                List<AIStructuredInput.SpecificationItem> specs = productSpecificationRepository
                                .findByProductIdOrderByDisplayOrderAsc(product.getId()).stream()
                                .map(s -> new AIStructuredInput.SpecificationItem(s.getAttributeName(),
                                                s.getAttributeValue()))
                                .toList();

                // Variant Attributes
                List<AIStructuredInput.AttributeItem> attrs = variantAttributeRepository
                                .findByVariantId(variant.getId()).stream()
                                .map(a -> new AIStructuredInput.AttributeItem(a.getAttributeName(),
                                                a.getAttributeValue()))
                                .toList();

                // Marketplace Comparison
                ProductComparisonResponse comparison = comparisonService.getComparisonForVariant(variant.getId());
                List<AIStructuredInput.OfferItem> offers = Collections.emptyList();
                if (comparison != null && comparison.getOffers() != null) {
                        BigDecimal minPrice = null;
                        for (ComparisonOfferResponse o : comparison.getOffers()) {
                                if (o.getCurrentPrice() != null) {
                                        if (minPrice == null || o.getCurrentPrice().compareTo(minPrice) < 0) {
                                                minPrice = o.getCurrentPrice();
                                        }
                                }
                        }
                        final BigDecimal lowestPrice = minPrice;

                        offers = comparison.getOffers().stream()
                                        .map((ComparisonOfferResponse o) -> {
                                                BigDecimal current = o.getCurrentPrice();
                                                BigDecimal original = o.getOriginalPrice();
                                                BigDecimal discountPct = null;
                                                if (current != null && original != null
                                                                && original.compareTo(BigDecimal.ZERO) > 0
                                                                && current.compareTo(original) <= 0) {
                                                        BigDecimal diff = original.subtract(current);
                                                        discountPct = diff.multiply(BigDecimal.valueOf(100))
                                                                        .divide(original, 1, RoundingMode.HALF_UP);
                                                }
                                                boolean isCheapest = (current != null && lowestPrice != null
                                                                && current.compareTo(lowestPrice) == 0);

                                                return AIStructuredInput.OfferItem.builder()
                                                                .platformName(o.getPlatform() != null
                                                                                ? o.getPlatform().getName()
                                                                                : "Unknown Platform")
                                                                .currentPrice(current)
                                                                .originalPrice(original)
                                                                .currency(o.getCurrency())
                                                                .sellerName(o.getSellerName())
                                                                .sellerRating(o.getSellerRating())
                                                                .availabilityStatus(o.getAvailabilityStatus())
                                                                .availabilityDetails(o.getAvailabilityDetails())
                                                                .deliveryInfo(o.getDeliveryInfo())
                                                                .offerDetails(o.getOfferDetails())
                                                                .discountPercentage(discountPct)
                                                                .isCheapest(isCheapest)
                                                                .build();
                                        })
                                        .toList();
                }

                // Stored Reviews
                VariantReviewsResponse reviewsResponse = reviewService.getReviewsForVariant(variant.getId(), null, 15);
                List<AIStructuredInput.ReviewItem> reviews = Collections.emptyList();
                if (reviewsResponse != null && reviewsResponse.getReviews() != null) {
                        reviews = reviewsResponse.getReviews().stream()
                                        .map((ReviewResponse r) -> AIStructuredInput.ReviewItem.builder()
                                                        .platformName(r.getPlatformName() != null ? r.getPlatformName()
                                                                        : "General")
                                                        .rating(r.getRating())
                                                        .title(r.getReviewTitle())
                                                        .text(r.getReviewText())
                                                        .verifiedPurchase(r.getVerifiedPurchase())
                                                        .reviewDate(r.getReviewDate() != null
                                                                        ? r.getReviewDate().toString()
                                                                        : null)
                                                        .build())
                                        .toList();
                }

                return AIStructuredInput.builder()
                                .variantId(variant.getId())
                                .productBrand(product.getBrand())
                                .productModel(product.getModel())
                                .categoryName(product.getCategory() != null ? product.getCategory().getName()
                                                : "General")
                                .productDescription(product.getDescription())
                                .variantName(variant.getVariantName())
                                .specifications(specs)
                                .variantAttributes(attrs)
                                .marketplaceOffers(offers)
                                .reviews(reviews)
                                .build();
        }

        private String buildPrompt(AIStructuredInput input) throws Exception {
                String inputJson = objectMapper.writeValueAsString(input);
                return """
                                You are ShopSense AI, an expert e-commerce marketplace comparison assistant.
                                Your PRIMARY objective is to provide a comprehensive, marketplace-focused comparison across the available selling platforms for the specified product variant.

                                CRITICAL RULES:
                                1. Evaluate and contrast ONLY the platforms listed in the input data. Use explicit platform names, exact price figures, discounts, delivery terms, and seller ratings supplied in marketplaceOffers.
                                2. "strengths" MUST focus primarily on platform-specific advantages (e.g., "Flipkart offers the lowest base price at ₹1,26,999 with a 5.8%% discount", "Amazon provides the fastest shipping via FREE One-Day Delivery", "Croma includes an additional flat ₹3,000 instant bank discount").
                                3. "drawbacks" MUST focus primarily on platform-specific trade-offs or limitations (e.g., "Croma lists a higher base price of ₹1,29,900", "Reliance Digital has a longer 2-day delivery window", "Croma has limited stock remaining").
                                4. "summary" MUST provide a clear overview comparing market pricing, best platform deals, and overall fulfillment options.
                                5. "valueAssessment" MUST compare current prices, MRP discounts, bank offers, and monetary value across the listed platforms.
                                6. "reviewInsights" MUST summarize platform-specific customer sentiment if reviews exist for that platform.
                                7. "bestOfferRecommendation" MUST explicitly state the top recommended marketplace option based on price, delivery, or bank cashback.
                                8. Do NOT invent prices, delivery times, seller ratings, or platform offers not present in the input data.
                                9. Focus strictly on the selected product variant: %s - %s.
                                10. Return ONLY a valid JSON object matching the following structure with no markdown wrapping:
                                {
                                  "summary": "Marketplace-focused summary comparing prices, deals, and delivery across platforms",
                                  "strengths": ["Platform advantage 1 with platform name and price/offer detail", "Platform advantage 2"],
                                  "drawbacks": ["Platform limitation/trade-off 1 with platform name", "Platform trade-off 2"],
                                  "valueAssessment": "Detailed marketplace price and value breakdown across platforms",
                                  "reviewInsights": "Platform-specific customer feedback and sentiment synthesis",
                                  "bestOfferRecommendation": "Clear recommendation declaring the best platform deal and why",
                                  "buyingGuidance": "Actionable advice on bank offers, shipping speed, and purchase strategy"
                                }

                                STRUCTURED INPUT DATA:
                                %s
                                """
                                .formatted(input.getProductBrand() + " " + input.getProductModel(),
                                                input.getVariantName(), inputJson);
        }

        private AIAnalysisResponse parseAndValidateResponse(String rawResponse, Long variantId, String productName,
                        String variantName) {
                try {
                        String cleaned = rawResponse.trim();
                        if (cleaned.startsWith("```json")) {
                                cleaned = cleaned.substring(7);
                        }
                        if (cleaned.startsWith("```")) {
                                cleaned = cleaned.substring(3);
                        }
                        if (cleaned.endsWith("```")) {
                                cleaned = cleaned.substring(0, cleaned.length() - 3);
                        }
                        cleaned = cleaned.trim();

                        JsonNode root = objectMapper.readTree(cleaned);

                        List<String> strengths = new ArrayList<>();
                        if (root.has("strengths") && root.get("strengths").isArray()) {
                                root.get("strengths").forEach(s -> strengths.add(s.asText()));
                        }

                        List<String> drawbacks = new ArrayList<>();
                        if (root.has("drawbacks") && root.get("drawbacks").isArray()) {
                                root.get("drawbacks").forEach(d -> drawbacks.add(d.asText()));
                        }

                        return AIAnalysisResponse.builder()
                                        .variantId(variantId)
                                        .productName(productName)
                                        .variantName(variantName)
                                        .summary(root.path("summary").asText("AI analysis completed successfully."))
                                        .strengths(strengths)
                                        .drawbacks(drawbacks)
                                        .valueAssessment(root.path("valueAssessment")
                                                        .asText("Value assessment provided based on listed offers."))
                                        .reviewInsights(root.path("reviewInsights")
                                                        .asText("Review insights derived from recent user feedback."))
                                        .bestOfferRecommendation(root.path("bestOfferRecommendation")
                                                        .asText("Refer to comparison offers above."))
                                        .buyingGuidance(root.path("buyingGuidance")
                                                        .asText("Compare prices and seller ratings before purchasing."))
                                        .generatedAt(LocalDateTime.now())
                                        .build();

                } catch (Exception e) {
                        log.warn("Failed to parse Gemini response as JSON. Falling back to default unavailable response structure. Cause: {}",
                                        e.getMessage());
                        return buildFallbackResponse(variantId, productName, variantName);
                }
        }

        private AIAnalysisResponse buildFallbackResponse(Long variantId, String productName, String variantName) {
                return AIAnalysisResponse.builder()
                                .variantId(variantId)
                                .productName(productName)
                                .variantName(variantName)
                                .summary("AI product analysis is temporarily unavailable for this variant.")
                                .strengths(List.of())
                                .drawbacks(List.of())
                                .valueAssessment("Value assessment is unavailable at this time.")
                                .reviewInsights("Review insights are unavailable at this time.")
                                .bestOfferRecommendation(
                                                "Please consult the live marketplace comparison table above for current offers.")
                                .buyingGuidance("Compare current marketplace prices and seller ratings directly.")
                                .generatedAt(LocalDateTime.now())
                                .build();
        }
}
