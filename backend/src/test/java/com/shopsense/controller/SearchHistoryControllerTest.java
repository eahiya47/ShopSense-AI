package com.shopsense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsense.dto.MessageResponse;
import com.shopsense.dto.SearchHistoryItemResponse;
import com.shopsense.dto.SearchHistoryListResponse;
import com.shopsense.dto.SearchHistoryRequest;
import com.shopsense.entity.User;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.security.CustomUserDetailsService;
import com.shopsense.security.JwtAuthenticationEntryPoint;
import com.shopsense.security.JwtTokenProvider;
import com.shopsense.security.UserPrincipal;
import com.shopsense.service.SearchHistoryService;
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

@WebMvcTest(SearchHistoryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SearchHistoryControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private SearchHistoryService searchHistoryService;

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
                                .name("Jane Doe")
                                .email("jane@example.com")
                                .password("encoded_pass")
                                .role("ROLE_USER")
                                .build();
                userPrincipal = UserPrincipal.create(user);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                                userPrincipal, null, userPrincipal.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);
        }

        @Test
        @DisplayName("POST /api/v1/search-history should return 200 OK on successful save")
        void testSaveSearchHistory_Success() throws Exception {
                SearchHistoryRequest request = SearchHistoryRequest.builder().query("iphone 16").build();
                MessageResponse response = new MessageResponse("Search history saved.");

                when(searchHistoryService.saveSearchHistory(eq(1L), any(SearchHistoryRequest.class)))
                                .thenReturn(response);

                mockMvc.perform(post("/api/v1/search-history")
                                .with(user(userPrincipal))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Search history saved."));
        }

        @Test
        @DisplayName("POST /api/v1/search-history should return 400 Bad Request when query is blank")
        void testSaveSearchHistory_BlankQuery() throws Exception {
                SearchHistoryRequest request = SearchHistoryRequest.builder().query("").build();

                mockMvc.perform(post("/api/v1/search-history")
                                .with(user(userPrincipal))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /api/v1/search-history should return 200 OK with history list")
        void testGetUserSearchHistory_Success() throws Exception {
                SearchHistoryItemResponse item = SearchHistoryItemResponse.builder()
                                .id(100L)
                                .query("iphone 16")
                                .searchedAt(LocalDateTime.now())
                                .build();

                SearchHistoryListResponse response = SearchHistoryListResponse.builder()
                                .history(List.of(item))
                                .build();

                when(searchHistoryService.getUserSearchHistory(1L)).thenReturn(response);

                mockMvc.perform(get("/api/v1/search-history")
                                .with(user(userPrincipal))
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.history[0].id").value(100))
                                .andExpect(jsonPath("$.history[0].query").value("iphone 16"));
        }

        @Test
        @DisplayName("DELETE /api/v1/search-history/{id} should return 204 No Content on successful deletion")
        void testDeleteSearchHistoryItem_Success() throws Exception {
                mockMvc.perform(delete("/api/v1/search-history/100")
                                .with(user(userPrincipal))
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("DELETE /api/v1/search-history/{id} should return 404 Not Found when entry is not found or not owned by user")
        void testDeleteSearchHistoryItem_NotFound() throws Exception {
                doThrow(new ResourceNotFoundException("Search history item not found with id: 999"))
                                .when(searchHistoryService).deleteSearchHistoryItem(1L, 999L);

                mockMvc.perform(delete("/api/v1/search-history/999")
                                .with(user(userPrincipal))
                                .with(csrf()))
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.status").value(404))
                                .andExpect(jsonPath("$.message").value("Search history item not found with id: 999"));
        }

        @Test
        @DisplayName("DELETE /api/v1/search-history should return 204 No Content on clearing all history for authenticated user")
        void testClearUserSearchHistory_Success() throws Exception {
                mockMvc.perform(delete("/api/v1/search-history")
                                .with(user(userPrincipal))
                                .with(csrf()))
                                .andExpect(status().isNoContent());
        }
}
