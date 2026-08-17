package com.shopsense.client.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

public class GeminiClientTest {

    private GeminiClientImpl geminiClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        geminiClient = new GeminiClientImpl(objectMapper);
    }

    @Test
    @DisplayName("Should return simulated JSON response when using mock API key")
    void testGenerateContent_WithMockKey() {
        ReflectionTestUtils.setField(geminiClient, "apiKey", "mock-key-for-dev");
        ReflectionTestUtils.setField(geminiClient, "apiUrl",
                "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent");

        String result = geminiClient.generateContent("Analyze iPhone 16 Pro");

        assertThat(result).isNotNull();
        assertThat(result).contains("summary");
        assertThat(result).contains("strengths");
    }
}
