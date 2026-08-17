package com.shopsense.controller;

import com.shopsense.dto.WishlistActionResponse;
import com.shopsense.dto.WishlistListResponse;
import com.shopsense.dto.WishlistRequest;
import com.shopsense.security.UserPrincipal;
import com.shopsense.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist Management", description = "Endpoints for managing user wishlist")
@SecurityRequirement(name = "bearerAuth")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    @Operation(summary = "Add product variant to wishlist", description = "Saves a product variant to the authenticated user's wishlist.")
    public ResponseEntity<WishlistActionResponse> addToWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody WishlistRequest request) {
        WishlistActionResponse response = wishlistService.addToWishlist(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get user wishlist", description = "Retrieves all wishlist items for the authenticated user.")
    public ResponseEntity<WishlistListResponse> getUserWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        WishlistListResponse response = wishlistService.getUserWishlist(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{productVariantId}")
    @Operation(summary = "Remove product variant from wishlist", description = "Removes a product variant from the authenticated user's wishlist.")
    public ResponseEntity<Void> removeFromWishlist(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long productVariantId) {
        wishlistService.removeFromWishlist(userPrincipal.getId(), productVariantId);
        return ResponseEntity.noContent().build();
    }
}
