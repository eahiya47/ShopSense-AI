package com.shopsense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.dto.WishlistActionResponse;
import com.shopsense.dto.WishlistItemResponse;
import com.shopsense.dto.WishlistListResponse;
import com.shopsense.dto.WishlistRequest;
import com.shopsense.entity.User;
import com.shopsense.exception.DuplicateResourceException;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.security.CustomUserDetailsService;
import com.shopsense.security.JwtAuthenticationEntryPoint;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.security.UserPrincipal;
import com.shopsense.service.WishlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WishlistController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WishlistControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private WishlistService wishlistService;

        @MockBean
        private JwtTokenProvider jwtTokenProvider;

        @MockBean
        private CustomUserDetailsService customUserDetailsService;

        @MockBean
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        private UserPrincipal userPrincipal;

        @BeforeEach
        void setUp() {
                User user = User.builder()
                                .id(1L)
                                .name("John Doe")
                                .email("john@example.com")
                                .password("encoded_pass")
                                .role("ROLE_USER")
                                .build();
                userPrincipal = UserPrincipal.create(user);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userPrincipal, null, userPrincipal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        @DisplayName("POST /api/v1/wishlist should return 201 Created on success")
        void testAddToWishlist_Success() throws Exception {
                WishlistRequest request = WishlistRequest.builder().productVariantId(1002L).build();
                WishlistActionResponse response = WishlistActionResponse.builder()
                                .message("Product added to wishlist.")
                                .productVariantId(1002L)
                                .build();

                when(wishlistService.addToWishlist(eq(1L), any(WishlistRequest.class))).thenReturn(response);

                mockMvc.perform(post("/api/v1/wishlist")
                                .with(user(userPrincipal))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.message").value("Product added to wishlist."))
                                .andExpect(jsonPath("$.productVariantId").value(1002));
        }

        @Test
        @DisplayName("POST /api/v1/wishlist should return 409 Conflict when item is duplicate")
        void testAddToWishlist_Duplicate() throws Exception {
                WishlistRequest request = WishlistRequest.builder().productVariantId(1002L).build();

                when(wishlistService.addToWishlist(eq(1L), any(WishlistRequest.class)))
                                .thenThrow(new DuplicateResourceException("Product is already in your wishlist."));

                mockMvc.perform(post("/api/v1/wishlist")
                                .with(user(userPrincipal))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.status").value(409))
                                .andExpect(jsonPath("$.error").value("RESOURCE_CONFLICT"))
                                .andExpect(jsonPath("$.message").value("Product is already in your wishlist."));
        }

        @Test
        @DisplayName("POST /api/v1/wishlist should return 400 Bad Request when request body is invalid")
        void testAddToWishlist_InvalidRequest() throws Exception {
                WishlistRequest request = new WishlistRequest(null);

                mockMvc.perform(post("/api/v1/wishlist")
                                .with(user(userPrincipal))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/v1/wishlist should return 200 OK with wishlist items")
        void testGetUserWishlist_Success() throws Exception {
                WishlistItemResponse item = WishlistItemResponse.builder()
                                .id(10L)
                                .productId(100L)
                                .productVariantId(1002L)
                                .productName("Apple iPhone 16 Pro")
                                .variantName("256GB / Natural Titanium")
                                .addedAt(LocalDateTime.now())
                                .build();

                WishlistListResponse response = WishlistListResponse.builder()
                                .items(List.of(item))
                                .build();

                when(wishlistService.getUserWishlist(1L)).thenReturn(response);

                mockMvc.perform(get("/api/v1/wishlist")
                                .with(user(userPrincipal))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.items[0].id").value(10))
                                .andExpect(jsonPath("$.items[0].productVariantId").value(1002))
                                .andExpect(jsonPath("$.items[0].productName").value("Apple iPhone 16 Pro"));
        }

        @Test
        @DisplayName("DELETE /api/v1/wishlist/{variantId} should return 204 No Content on successful deletion")
        void testRemoveFromWishlist_Success() throws Exception {
                mockMvc.perform(delete("/api/v1/wishlist/1002")
                                .with(user(userPrincipal))
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /api/v1/wishlist/{variantId} should return 404 Not Found when wishlist item does not exist")
        void testRemoveFromWishlist_NotFound() throws Exception {
                doThrow(new ResourceNotFoundException("Wishlist item not found for product variant id: 9999"))
                                .when(wishlistService).removeFromWishlist(1L, 9999L);

                mockMvc.perform(delete("/api/v1/wishlist/9999")
                                .with(user(userPrincipal))
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message")
                                                .value("Wishlist item not found for product variant id: 9999"));
        }
}
