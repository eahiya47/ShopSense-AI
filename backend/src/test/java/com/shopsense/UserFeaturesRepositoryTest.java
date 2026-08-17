package com.shopsense;

import com.shopsense.entity.*;
import com.shopsense.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
public class UserFeaturesRepositoryTest {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private ProductRepository productRepository;

        @Autowired
        private ProductVariantRepository productVariantRepository;

        @Autowired
        private WishlistRepository wishlistRepository;

        @Autowired
        private SearchHistoryRepository searchHistoryRepository;

        @Test
        @DisplayName("Should save wishlist entry and enforce database uniqueness constraint")
        void testWishlistRepositoryOperations() {
                User user = userRepository.save(User.builder()
                                .name("John Doe")
                                .email("john.wishlist@example.com")
                                .password("encoded_pass")
                                .role("ROLE_USER")
                                .build());

                Category category = categoryRepository.save(Category.builder().name("Electronics").build());
                Product product = productRepository.save(Product.builder()
                                .brand("Apple")
                                .model("iPhone 16 Pro")
                                .category(category)
                                .build());

                ProductVariant variant = productVariantRepository.save(ProductVariant.builder()
                                .product(product)
                                .variantName("256GB / Natural Titanium")
                                .build());

                Wishlist wishlist = Wishlist.builder()
                                .user(user)
                                .productVariant(variant)
                                .build();

                Wishlist savedWishlist = wishlistRepository.save(wishlist);
                assertThat(savedWishlist.getId()).isNotNull();

                assertThat(wishlistRepository.existsByUserIdAndProductVariantId(user.getId(), variant.getId()))
                                .isTrue();

                List<Wishlist> userWishlist = wishlistRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
                assertThat(userWishlist).hasSize(1);
                assertThat(userWishlist.get(0).getProductVariant().getId()).isEqualTo(variant.getId());

                // Enforce DB Uniqueness Constraint
                Wishlist duplicateWishlist = Wishlist.builder()
                                .user(user)
                                .productVariant(variant)
                                .build();

                assertThrows(DataIntegrityViolationException.class, () -> {
                        wishlistRepository.saveAndFlush(duplicateWishlist);
                });
        }

        @Test
        @DisplayName("Should save, retrieve, and delete search history entries for authenticated user")
        void testSearchHistoryRepositoryOperations() {
                User user = userRepository.save(User.builder()
                                .name("Jane Doe")
                                .email("jane.search@example.com")
                                .password("encoded_pass")
                                .role("ROLE_USER")
                                .build());

                SearchHistory sh1 = searchHistoryRepository.save(SearchHistory.builder()
                                .user(user)
                                .searchQuery("iphone 16")
                                .build());

                SearchHistory sh2 = searchHistoryRepository.save(SearchHistory.builder()
                                .user(user)
                                .searchQuery("gaming laptop")
                                .build());

                assertThat(sh1.getId()).isNotNull();
                assertThat(sh2.getId()).isNotNull();

                List<SearchHistory> history = searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(user.getId());
                assertThat(history).hasSize(2);

                Optional<SearchHistory> found = searchHistoryRepository.findByIdAndUserId(sh1.getId(), user.getId());
                assertThat(found).isPresent();
                assertThat(found.get().getSearchQuery()).isEqualTo("iphone 16");

                // Verify isolation by another user
                User otherUser = userRepository.save(User.builder()
                                .name("Other User")
                                .email("other.user@example.com")
                                .password("encoded_pass")
                                .role("ROLE_USER")
                                .build());

                Optional<SearchHistory> wrongUserFind = searchHistoryRepository.findByIdAndUserId(sh1.getId(),
                                otherUser.getId());
                assertThat(wrongUserFind).isEmpty();
        }

        @Test
        @DisplayName("Should bulk delete all search history entries belonging ONLY to specified user")
        void testDeleteByUserId_UserIsolation() {
                User userA = userRepository.save(User.builder()
                                .name("User A")
                                .email("usera@example.com")
                                .password("encoded_pass")
                                .role("ROLE_USER")
                                .build());

                User userB = userRepository.save(User.builder()
                                .name("User B")
                                .email("userb@example.com")
                                .password("encoded_pass")
                                .role("ROLE_USER")
                                .build());

                searchHistoryRepository.save(SearchHistory.builder().user(userA).searchQuery("User A query 1").build());
                searchHistoryRepository.save(SearchHistory.builder().user(userA).searchQuery("User A query 2").build());
                searchHistoryRepository.save(SearchHistory.builder().user(userB).searchQuery("User B query 1").build());

                assertThat(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userA.getId())).hasSize(2);
                assertThat(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userB.getId())).hasSize(1);

                searchHistoryRepository.deleteByUserId(userA.getId());

                assertThat(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userA.getId())).isEmpty();
                assertThat(searchHistoryRepository.findByUserIdOrderBySearchedAtDesc(userB.getId())).hasSize(1);
        }
}
