package com.shopsense.client.gemini;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
        return """
                {
                  "summary": "This variant offers an excellent balance of high performance, premium build quality, and competitive pricing across major marketplaces.",
                  "strengths": [
                    "Top-tier performance and display quality",
                    "Highly competitive pricing on available platforms",
                    "Strong customer feedback regarding build and feature set"
                  ],
                  "drawbacks": [
                    "Higher base price relative to non-pro models",
                    "Limited stock duration reported on select seller listings"
                  ],
                  "valueAssessment": "Provides solid long-term value for power users needing premium features.",
                  "reviewInsights": "Recent customer feedback highlights excellent reliability, fast responsiveness, and overall satisfaction.",
                  "bestOfferRecommendation": "Check the marketplace offering the lowest current listed price with verified seller ratings.",
                  "buyingGuidance": "Compare immediate availability and delivery estimates before placing an order."
                }
                """;
    }
}
