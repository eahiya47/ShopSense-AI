package com.shopsense.controller;

import com.shopsense.dto.*;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.security.CustomUserDetailsService;
import com.shopsense.security.JwtAuthenticationEntryPoint;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/reviews should be publicly accessible and return 200 OK")
    void testGetVariantReviews_Success() throws Exception {
        PlatformResponse platformResponse = PlatformResponse.builder()
                .id(1L)
                .name("Amazon")
                .websiteUrl("https://www.amazon.in")
                .build();

        ReviewResponse reviewResponse = ReviewResponse.builder()
                .id(10L)
                .reviewerName("Jane Doe")
                .rating(BigDecimal.valueOf(5.0))
                .reviewTitle("Outstanding camera")
                .reviewText("Extremely satisfied with the build quality and camera.")
                .reviewDate(LocalDate.of(2026, 8, 1))
                .verifiedPurchase(true)
                .sourceUrl("https://www.amazon.in/review/10")
                .fetchedAt(LocalDateTime.now())
                .build();

        VariantReviewsResponse reviewsResponse = VariantReviewsResponse.builder()
                .variantId(1002L)
                .platform(platformResponse)
                .reviews(List.of(reviewResponse))
                .totalReviews(1)
                .lastUpdated(LocalDateTime.now())
                .build();

        when(reviewService.getReviewsForVariant(1002L, 1L, 15)).thenReturn(reviewsResponse);

        mockMvc.perform(get("/api/v1/variants/1002/reviews?platformId=1&limit=15")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value(1002))
                .andExpect(jsonPath("$.platform.name").value("Amazon"))
                .andExpect(jsonPath("$.reviews[0].reviewerName").value("Jane Doe"))
                .andExpect(jsonPath("$.reviews[0].rating").value(5.0))
                .andExpect(jsonPath("$.totalReviews").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/variants/{variantId}/reviews should return 404 NOT FOUND for missing variant")
    void testGetVariantReviews_NotFound() throws Exception {
        when(reviewService.getReviewsForVariant(9999L, null, 15))
                .thenThrow(new ResourceNotFoundException("Product variant not found with id: 9999"));

        mockMvc.perform(get("/api/v1/variants/9999/reviews")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product variant not found with id: 9999"));
    }
}
