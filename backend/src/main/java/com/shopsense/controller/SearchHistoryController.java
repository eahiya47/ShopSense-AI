package com.shopsense.controller;

import com.shopsense.dto.MessageResponse;
import com.shopsense.dto.SearchHistoryListResponse;
import com.shopsense.dto.SearchHistoryRequest;
import com.shopsense.security.UserPrincipal;
import com.shopsense.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search-history")
@RequiredArgsConstructor
@Tag(name = "Search History Management", description = "Endpoints for managing user search history")
@SecurityRequirement(name = "bearerAuth")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @PostMapping
    @Operation(summary = "Save search history entry", description = "Saves a search query to the authenticated user's search history.")
    public ResponseEntity<MessageResponse> saveSearchHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody SearchHistoryRequest request) {
        MessageResponse response = searchHistoryService.saveSearchHistory(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get user search history", description = "Retrieves search history entries for the authenticated user.")
    public ResponseEntity<SearchHistoryListResponse> getUserSearchHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        SearchHistoryListResponse response = searchHistoryService.getUserSearchHistory(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete search history entry", description = "Deletes a specific search history entry owned by the authenticated user.")
    public ResponseEntity<Void> deleteSearchHistoryItem(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long id) {
        searchHistoryService.deleteSearchHistoryItem(userPrincipal.getId(), id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    @Operation(summary = "Clear all search history", description = "Deletes all search history entries owned by the authenticated user.")
    public ResponseEntity<Void> clearUserSearchHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        searchHistoryService.clearUserSearchHistory(userPrincipal.getId());
        return ResponseEntity.noContent().build();
    }
}
