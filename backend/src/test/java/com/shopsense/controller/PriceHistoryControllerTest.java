package com.shopsense.controller;

import com.shopsense.dto.PriceHistoryItemResponse;
import com.shopsense.dto.PriceHistoryResponse;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.security.CustomUserDetailsService;
import com.shopsense.security.JwtAuthenticationEntryPoint;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.service.PriceHistoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PriceHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PriceHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceHistoryService priceHistoryService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/price-history should return 200 OK with price history response")
    void testGetPriceHistory_Success() throws Exception {
        PriceHistoryItemResponse item = PriceHistoryItemResponse.builder()
                .id(1L)
                .platformId(1L)
                .platformName("Amazon")
                .price(BigDecimal.valueOf(127990))
                .originalPrice(BigDecimal.valueOf(134900))
                .currency("INR")
                .recordedAt(LocalDateTime.now())
                .build();

        PriceHistoryResponse response = PriceHistoryResponse.builder()
                .variantId(101L)
                .variantName("256GB - Natural Titanium")
                .history(List.of(item))
                .build();

        when(priceHistoryService.getPriceHistoryForVariant(eq(101L), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/variants/101/price-history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value(101))
                .andExpect(jsonPath("$.variantName").value("256GB - Natural Titanium"))
                .andExpect(jsonPath("$.history[0].platformName").value("Amazon"))
                .andExpect(jsonPath("$.history[0].price").value(127990));
    }

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/price-history should return 200 OK with empty history")
    void testGetPriceHistory_EmptyHistory() throws Exception {
        PriceHistoryResponse response = PriceHistoryResponse.builder()
                .variantId(101L)
                .variantName("256GB - Natural Titanium")
                .history(Collections.emptyList())
                .build();

        when(priceHistoryService.getPriceHistoryForVariant(eq(101L), any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/variants/101/price-history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value(101))
                .andExpect(jsonPath("$.history").isEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/price-history should return 404 Not Found for invalid variant ID")
    void testGetPriceHistory_NotFound() throws Exception {
        when(priceHistoryService.getPriceHistoryForVariant(eq(9999L), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Product variant not found with id: 9999"));

        mockMvc.perform(get("/api/v1/variants/9999/price-history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product variant not found with id: 9999"));
    }
}
