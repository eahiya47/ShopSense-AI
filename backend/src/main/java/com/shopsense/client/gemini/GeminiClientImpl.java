package com.shopsense.client.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.dto.ai.AIStructuredInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GeminiClientImpl implements GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final ObjectMapper objectMapper;

    @Override
    public String generateContent(String prompt) {
        if (apiKey == null || apiKey.isBlank() || "mock-key-for-dev".equals(apiKey)) {
            log.warn("Gemini API key is not configured or using mock key. Returning simulated response.");
            return simulateGeminiResponse(prompt);
        }

        try {
            RestTemplate restTemplate = new RestTemplate();

            String fullUrl = apiUrl + "?key=" + apiKey;

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)))));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    fullUrl,
                    HttpMethod.POST,
                    entity,
                    String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode candidates = root.path("candidates");
                if (candidates.isArray() && !candidates.isEmpty()) {
                    JsonNode textNode = candidates.get(0).path("content").path("parts").get(0).path("text");
                    if (!textNode.isMissingNode()) {
                        return textNode.asText();
                    }
                }
            }

            throw new RuntimeException("Unexpected response format from Gemini API");

        } catch (Exception e) {
            log.error("Error communicating with Gemini API: {}", e.getMessage());
            throw new RuntimeException("Gemini service communication failure", e);
        }
    }

    private String simulateGeminiResponse(String prompt) {
        if (prompt != null) {
            try {
                int jsonIdx = prompt.lastIndexOf("STRUCTURED INPUT DATA:");
                if (jsonIdx != -1) {
                    String jsonStr = prompt.substring(jsonIdx + "STRUCTURED INPUT DATA:".length()).trim();
                    AIStructuredInput input = objectMapper.readValue(jsonStr, AIStructuredInput.class);
                    if (input != null && input.getMarketplaceOffers() != null
                            && !input.getMarketplaceOffers().isEmpty()) {
                        return buildDynamicMockResponse(input);
                    }
                }
            } catch (Exception e) {
                log.warn(
                        "Could not parse AIStructuredInput in simulateGeminiResponse. Falling back to default mock response. Cause: {}",
                        e.getMessage());
            }
        }
        return simulateDefaultFallbackResponse();
    }

    private String buildDynamicMockResponse(AIStructuredInput input) {
        List<AIStructuredInput.OfferItem> offers = input.getMarketplaceOffers();

        AIStructuredInput.OfferItem cheapest = offers.stream()
                .filter(o -> o.getCurrentPrice() != null)
                .min((o1, o2) -> o1.getCurrentPrice().compareTo(o2.getCurrentPrice()))
                .orElse(offers.get(0));

        AIStructuredInput.OfferItem highestPrice = offers.stream()
                .filter(o -> o.getCurrentPrice() != null)
                .max((o1, o2) -> o1.getCurrentPrice().compareTo(o2.getCurrentPrice()))
                .orElse(offers.get(0));

        AIStructuredInput.OfferItem highestRated = offers.stream()
                .filter(o -> o.getSellerRating() != null)
                .max((o1, o2) -> Double.compare(o1.getSellerRating(), o2.getSellerRating()))
                .orElse(null);

        List<String> strengths = new ArrayList<>();
        List<String> drawbacks = new ArrayList<>();

        if (cheapest != null && cheapest.getCurrentPrice() != null) {
            String discountStr = cheapest.getDiscountPercentage() != null
                    ? " (" + cheapest.getDiscountPercentage() + "% discount)"
                    : "";
            strengths.add(cheapest.getPlatformName() + " offers the lowest current price at "
                    + formatPrice(cheapest.getCurrentPrice(), cheapest.getCurrency()) + discountStr + ".");
        }

        if (highestRated != null && highestRated.getSellerRating() != null && !highestRated.equals(cheapest)) {
            strengths.add(highestRated.getPlatformName() + " features the top seller rating ("
                    + highestRated.getSellerName() + " rated " + highestRated.getSellerRating() + "/5.0).");
        } else if (cheapest != null && cheapest.getSellerRating() != null) {
            strengths.add(cheapest.getPlatformName() + " features a strong seller rating of "
                    + cheapest.getSellerRating() + "/5.0.");
        }

        for (AIStructuredInput.OfferItem o : offers) {
            if (o.getDeliveryInfo() != null && (o.getDeliveryInfo().toLowerCase().contains("one-day")
                    || o.getDeliveryInfo().toLowerCase().contains("express")
                    || o.getDeliveryInfo().toLowerCase().contains("today"))) {
                strengths.add(o.getPlatformName() + " offers expedited fulfillment: " + o.getDeliveryInfo() + ".");
                break;
            }
        }

        for (AIStructuredInput.OfferItem o : offers) {
            if (o.getOfferDetails() != null && !o.getOfferDetails().isBlank()) {
                strengths.add(o.getPlatformName() + " bank deal: " + o.getOfferDetails() + ".");
                break;
            }
        }

        if (highestPrice != null && cheapest != null && !highestPrice.equals(cheapest)
                && highestPrice.getCurrentPrice() != null && cheapest.getCurrentPrice() != null) {
            BigDecimal diff = highestPrice.getCurrentPrice().subtract(cheapest.getCurrentPrice());
            drawbacks.add(highestPrice.getPlatformName() + " lists a higher base price of "
                    + formatPrice(highestPrice.getCurrentPrice(), highestPrice.getCurrency())
                    + " (" + formatPrice(diff, highestPrice.getCurrency()) + " higher than "
                    + cheapest.getPlatformName() + ").");
        }

        for (AIStructuredInput.OfferItem o : offers) {
            if (o.getAvailabilityDetails() != null && o.getAvailabilityDetails().toLowerCase().contains("remaining")) {
                drawbacks.add(o.getPlatformName() + " stock advisory: " + o.getAvailabilityDetails() + ".");
            } else if (o.getDeliveryInfo() != null && o.getDeliveryInfo().toLowerCase().contains("2 days")) {
                drawbacks.add(o.getPlatformName() + " provides standard 2-day delivery timeframes.");
            }
        }

        if (drawbacks.isEmpty()) {
            drawbacks.add("Marketplace prices and offer availability may change rapidly based on seller stock levels.");
        }

        String platformNamesStr = offers.stream()
                .map(AIStructuredInput.OfferItem::getPlatformName)
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("sellers");

        String summaryStr = String.format(
                "Multi-platform offer comparison for %s %s (%s): Currently available across %d marketplace(s) (%s). %s provides the lowest listed price at %s.",
                input.getProductBrand() != null ? input.getProductBrand() : "Product",
                input.getProductModel() != null ? input.getProductModel() : "",
                input.getVariantName() != null ? input.getVariantName() : "Standard",
                offers.size(),
                platformNamesStr,
                cheapest != null ? cheapest.getPlatformName() : "The leading seller",
                cheapest != null ? formatPrice(cheapest.getCurrentPrice(), cheapest.getCurrency())
                        : "competitive rates");

        String valueAssessmentStr = String.format(
                "Listed prices range from %s on %s up to %s on %s. %s offers the strongest initial upfront value.",
                cheapest != null ? formatPrice(cheapest.getCurrentPrice(), cheapest.getCurrency()) : "N/A",
                cheapest != null ? cheapest.getPlatformName() : "N/A",
                highestPrice != null ? formatPrice(highestPrice.getCurrentPrice(), highestPrice.getCurrency()) : "N/A",
                highestPrice != null ? highestPrice.getPlatformName() : "N/A",
                cheapest != null ? cheapest.getPlatformName() : "The top platform");

        String reviewInsightsStr = (input.getReviews() != null && !input.getReviews().isEmpty())
                ? String.format(
                        "Analyzed %d customer review(s) across platforms. Feedback highlights high product satisfaction, reliable fulfillment, and seller credibility on %s.",
                        input.getReviews().size(), platformNamesStr)
                : "Customer feedback across listed platforms indicates general product satisfaction and reliable seller delivery.";

        String bestOfferStr = String.format(
                "Best overall offer: %s at %s (%s, Seller: %s).",
                cheapest != null ? cheapest.getPlatformName() : "Leading seller",
                cheapest != null ? formatPrice(cheapest.getCurrentPrice(), cheapest.getCurrency()) : "listed price",
                cheapest != null && cheapest.getDeliveryInfo() != null ? cheapest.getDeliveryInfo()
                        : "Standard delivery",
                cheapest != null && cheapest.getSellerName() != null ? cheapest.getSellerName() : "Verified seller");

        String buyingGuidanceStr = "Compare card-specific bank cashbacks, instant checkout discounts, and shipping speeds before placing your final order.";

        try {
            Map<String, Object> respMap = Map.of(
                    "summary", summaryStr,
                    "strengths", strengths,
                    "drawbacks", drawbacks,
                    "valueAssessment", valueAssessmentStr,
                    "reviewInsights", reviewInsightsStr,
                    "bestOfferRecommendation", bestOfferStr,
                    "buyingGuidance", buyingGuidanceStr);
            return objectMapper.writeValueAsString(respMap);
        } catch (Exception e) {
            return simulateDefaultFallbackResponse();
        }
    }

    private String formatPrice(BigDecimal price, String currency) {
        if (price == null)
            return "N/A";
        String symbol = ("INR".equalsIgnoreCase(currency) || "₹".equals(currency)) ? "₹"
                : (currency != null ? currency + " " : "");
        return symbol + String.format("%,.2f", price);
    }

    private String simulateDefaultFallbackResponse() {
        return """
                {
                  "summary": "This variant is listed across multiple active marketplaces with varying prices and delivery options.",
                  "strengths": [
                    "Available across verified online platforms",
                    "Includes platform-specific seller and warranty details"
                  ],
                  "drawbacks": [
                    "Price differences exist between participating sellers"
                  ],
                  "valueAssessment": "Compare current pricing and promotional cashback before making a purchase decision.",
                  "reviewInsights": "Customer feedback reflects positive satisfaction with overall performance.",
                  "bestOfferRecommendation": "Select the platform providing the best price and shipping terms.",
                  "buyingGuidance": "Verify stock status and delivery estimates prior to placing an order."
                }
                """;
    }
}
