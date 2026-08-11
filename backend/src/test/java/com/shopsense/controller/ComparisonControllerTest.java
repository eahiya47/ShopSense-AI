package com.shopsense.controller;

import com.shopsense.dto.*;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.security.CustomUserDetailsService;
import com.shopsense.security.JwtAuthenticationEntryPoint;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.service.ComparisonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ComparisonController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ComparisonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ComparisonService comparisonService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/comparison should be publicly accessible and return 200 OK")
    void testGetProductComparison_Success() throws Exception {
        ProductVariantResponse variantResponse = ProductVariantResponse.builder()
                .id(1002L)
                .productId(100L)
                .name("256GB / Natural Titanium")
                .isDefault(false)
                .attributes(Collections.emptyList())
                .build();

        PlatformResponse platformResponse = PlatformResponse.builder()
                .id(1L)
                .name("Amazon")
                .websiteUrl("https://www.amazon.in")
                .build();

        ComparisonOfferResponse offerResponse = ComparisonOfferResponse.builder()
                .platform(platformResponse)
                .currentPrice(BigDecimal.valueOf(114999))
                .originalPrice(BigDecimal.valueOf(119999))
                .currency("INR")
                .sellerName("Amazon Retail")
                .sellerRating(4.7)
                .availabilityStatus("IN_STOCK")
                .deliveryInfo("Delivery tomorrow")
                .productUrl("https://www.amazon.in/dp/mock")
                .build();

        PlatformStatusResponse statusResponse = PlatformStatusResponse.builder()
                .platform("Amazon")
                .status("AVAILABLE")
                .build();

        ProductComparisonResponse comparisonResponse = ProductComparisonResponse.builder()
                .variant(variantResponse)
                .offers(List.of(offerResponse))
                .platformStatus(List.of(statusResponse))
                .build();

        when(comparisonService.getComparisonForVariant(1002L)).thenReturn(comparisonResponse);

        mockMvc.perform(get("/api/v1/variants/1002/comparison")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variant.id").value(1002))
                .andExpect(jsonPath("$.variant.productId").value(100))
                .andExpect(jsonPath("$.offers[0].platform.name").value("Amazon"))
                .andExpect(jsonPath("$.offers[0].currentPrice").value(114999))
                .andExpect(jsonPath("$.platformStatus[0].status").value("AVAILABLE"));
    }

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/comparison should return 404 NOT FOUND for missing variant")
    void testGetProductComparison_NotFound() throws Exception {
        when(comparisonService.getComparisonForVariant(9999L))
                .thenThrow(new ResourceNotFoundException("Product variant not found with id: 9999"));

        mockMvc.perform(get("/api/v1/variants/9999/comparison")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product variant not found with id: 9999"));
    }
}
