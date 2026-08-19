package com.shopsense.service;

import com.shopsense.dto.WishlistActionResponse;
import com.shopsense.dto.WishlistItemResponse;
import com.shopsense.dto.WishlistListResponse;
import com.shopsense.dto.WishlistRequest;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.User;
import com.shopsense.entity.Wishlist;
import com.shopsense.exception.DuplicateResourceException;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.UserRepository;
import com.shopsense.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    @Transactional
    public WishlistActionResponse addToWishlist(Long userId, WishlistRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product variant not found with id: " + request.getProductVariantId()));

        if (wishlistRepository.existsByUserIdAndProductVariantId(userId, request.getProductVariantId())) {
            throw new DuplicateResourceException("Product is already in your wishlist.");
        }

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .productVariant(variant)
                .build();

        wishlistRepository.save(wishlist);

        return WishlistActionResponse.builder()
                .message("Product added to wishlist.")
                .productVariantId(variant.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public WishlistListResponse getUserWishlist(Long userId) {
        List<Wishlist> wishlistItems = wishlistRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<WishlistItemResponse> items = wishlistItems.stream()
                .map(this::mapToWishlistItemResponse)
                .collect(Collectors.toList());

        return WishlistListResponse.builder()
                .items(items)
                .build();
    }

    @Override
    @Transactional
    public void removeFromWishlist(Long userId, Long productVariantId) {
        Wishlist item = wishlistRepository.findByUserIdAndProductVariantId(userId, productVariantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Wishlist item not found for product variant id: " + productVariantId));

        wishlistRepository.delete(item);
    }

    private WishlistItemResponse mapToWishlistItemResponse(Wishlist wishlist) {
        ProductVariant variant = wishlist.getProductVariant();
        Product product = variant.getProduct();

        StringBuilder nameBuilder = new StringBuilder();
        if (product.getBrand() != null && !product.getBrand().isBlank()) {
            nameBuilder.append(product.getBrand()).append(" ");
        }
        if (product.getSeries() != null && !product.getSeries().isBlank()) {
            nameBuilder.append(product.getSeries()).append(" ");
        }
        if (product.getModel() != null) {
            nameBuilder.append(product.getModel());
        }
        String productName = nameBuilder.toString().trim();

        return WishlistItemResponse.builder()
                .id(wishlist.getId())
                .productId(product.getId())
                .productVariantId(variant.getId())
                .productName(productName)
                .variantName(variant.getVariantName())
                .addedAt(wishlist.getCreatedAt())
                .build();
    }
}
