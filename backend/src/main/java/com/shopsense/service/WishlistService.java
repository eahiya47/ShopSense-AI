package com.shopsense.service;

import com.shopsense.dto.WishlistActionResponse;
import com.shopsense.dto.WishlistListResponse;
import com.shopsense.dto.WishlistRequest;

public interface WishlistService {

    WishlistActionResponse addToWishlist(Long userId, WishlistRequest request);

    WishlistListResponse getUserWishlist(Long userId);

    void removeFromWishlist(Long userId, Long productVariantId);
}
