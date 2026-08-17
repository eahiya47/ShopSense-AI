package com.shopsense.service;

import com.shopsense.dto.WishlistActionResponse;
import com.shopsense.dto.WishlistListResponse;
import com.shopsense.dto.WishlistRequest;
import com.shopsense.entity.Category;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.User;
import com.shopsense.entity.Wishlist;
import com.shopsense.exception.DuplicateResourceException;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.UserRepository;
import com.shopsense.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishlistServiceTest {

    @Mock
    private WishlistRepository wishlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @InjectMocks
    private WishlistServiceImpl wishlistService;

    private User sampleUser;
    private ProductVariant sampleVariant;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@example.com")
                .build();

        Category category = Category.builder().id(10L).name("Smartphones").build();
        Product product = Product.builder()
                .id(100L)
                .brand("Apple")
                .series("iPhone")
                .model("16 Pro")
                .category(category)
                .build();

        sampleVariant = ProductVariant.builder()
                .id(1002L)
                .product(product)
                .variantName("256GB / Natural Titanium")
                .build();
    }

    @Test
    @DisplayName("Should successfully add product variant to wishlist")
    void testAddToWishlist_Success() {
        WishlistRequest request = WishlistRequest.builder().productVariantId(1002L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(wishlistRepository.existsByUserIdAndProductVariantId(1L, 1002L)).thenReturn(false);

        WishlistActionResponse response = wishlistService.addToWishlist(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getProductVariantId()).isEqualTo(1002L);
        assertThat(response.getMessage()).isEqualTo("Product added to wishlist.");

        verify(wishlistRepository, times(1)).save(any(Wishlist.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when item is already in wishlist")
    void testAddToWishlist_Duplicate() {
        WishlistRequest request = WishlistRequest.builder().productVariantId(1002L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productVariantRepository.findById(1002L)).thenReturn(Optional.of(sampleVariant));
        when(wishlistRepository.existsByUserIdAndProductVariantId(1L, 1002L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> wishlistService.addToWishlist(1L, request));
        verify(wishlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when variant does not exist")
    void testAddToWishlist_VariantNotFound() {
        WishlistRequest request = WishlistRequest.builder().productVariantId(9999L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
        when(productVariantRepository.findById(9999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.addToWishlist(1L, request));
    }

    @Test
    @DisplayName("Should return wishlist items for authenticated user")
    void testGetUserWishlist_Success() {
        Wishlist item = Wishlist.builder()
                .id(10L)
                .user(sampleUser)
                .productVariant(sampleVariant)
                .createdAt(LocalDateTime.now())
                .build();

        when(wishlistRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(List.of(item));

        WishlistListResponse response = wishlistService.getUserWishlist(1L);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("Apple iPhone 16 Pro");
        assertThat(response.getItems().get(0).getVariantName()).isEqualTo("256GB / Natural Titanium");
    }

    @Test
    @DisplayName("Should remove wishlist item owned by user")
    void testRemoveFromWishlist_Success() {
        Wishlist item = Wishlist.builder()
                .id(10L)
                .user(sampleUser)
                .productVariant(sampleVariant)
                .build();

        when(wishlistRepository.findByUserIdAndProductVariantId(1L, 1002L)).thenReturn(Optional.of(item));

        wishlistService.removeFromWishlist(1L, 1002L);

        verify(wishlistRepository, times(1)).delete(item);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when removing item not in user's wishlist")
    void testRemoveFromWishlist_NotFound() {
        when(wishlistRepository.findByUserIdAndProductVariantId(1L, 1002L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> wishlistService.removeFromWishlist(1L, 1002L));
        verify(wishlistRepository, never()).delete(any());
    }
}
