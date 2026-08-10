package com.shopsense.controller;

import com.shopsense.dto.*;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.security.CustomUserDetailsService;
import com.shopsense.security.JwtAuthenticationEntryPoint;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProductControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private ProductService productService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @Test
        @DisplayName("GET /api/v1/products/search should be publicly accessible and return 200 OK")
        void testSearchProducts_PublicAccess() throws Exception {
                ProductSummaryResponse summary = ProductSummaryResponse.builder()
                                .id(101L)
                                .brand("Apple")
                                .series("iPhone")
                                .model("16 Pro")
                                .category("Smartphone")
                                .hasVariants(true)
                                .build();

                ProductSearchResponse searchResponse = ProductSearchResponse.builder()
                                .query("iphone")
                                .page(0)
                                .size(20)
                                .totalResults(1L)
                                .totalPages(1)
                                .products(List.of(summary))
                                .build();

                when(productService.searchProducts(any(), any(), anyInt(), anyInt())).thenReturn(searchResponse);

                mockMvc.perform(get("/api/v1/products/search")
                                .param("q", "iphone")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.query").value("iphone"))
                                .andExpect(jsonPath("$.totalResults").value(1))
                                .andExpect(jsonPath("$.products[0].model").value("16 Pro"));
        }

        @Test
        @DisplayName("GET /api/v1/products/{id} should return 200 OK for valid product")
        void testGetProductById_Success() throws Exception {
                CategoryResponse category = CategoryResponse.builder().id(10L).name("Smartphone").build();
                ProductDetailResponse detail = ProductDetailResponse.builder()
                                .id(101L)
                                .brand("Apple")
                                .model("16 Pro")
                                .category(category)
                                .specifications(Collections.emptyList())
                                .build();

                when(productService.getProductById(101L)).thenReturn(detail);

                mockMvc.perform(get("/api/v1/products/101")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(101))
                                .andExpect(jsonPath("$.brand").value("Apple"));
        }

        @Test
        @DisplayName("GET /api/v1/products/{id} should return 404 NOT FOUND for missing product")
        void testGetProductById_NotFound() throws Exception {
                when(productService.getProductById(999L))
                                .thenThrow(new ResourceNotFoundException("Product not found with id: 999"));

                mockMvc.perform(get("/api/v1/products/999")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").value("Product not found with id: 999"));
        }

        @Test
        @DisplayName("GET /api/v1/products/{id}/variants should return 200 OK with variants list")
        void testGetProductVariants_Success() throws Exception {
                ProductVariantResponse variant = ProductVariantResponse.builder()
                                .id(201L)
                                .name("256GB / Natural Titanium")
                                .isDefault(false)
                                .attributes(Collections.emptyList())
                                .build();

                ProductVariantListResponse response = ProductVariantListResponse.builder()
                                .productId(101L)
                                .variants(List.of(variant))
                                .build();

                when(productService.getProductVariants(101L)).thenReturn(response);

                mockMvc.perform(get("/api/v1/products/101/variants")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.productId").value(101))
                                .andExpect(jsonPath("$.variants[0].name").value("256GB / Natural Titanium"));
        }
}
