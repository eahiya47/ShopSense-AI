package com.shopsense.service;

import com.shopsense.dto.ProductDetailResponse;
import com.shopsense.dto.ProductSearchResponse;
import com.shopsense.dto.ProductVariantListResponse;
import com.shopsense.entity.Category;
import com.shopsense.entity.Product;
import com.shopsense.entity.ProductSpecification;
import com.shopsense.entity.ProductVariant;
import com.shopsense.entity.VariantAttribute;
import com.shopsense.exception.BadRequestException;
import com.shopsense.exception.ResourceNotFoundException;
import com.shopsense.repository.ProductRepository;
import com.shopsense.repository.ProductSpecificationRepository;
import com.shopsense.repository.ProductVariantRepository;
import com.shopsense.repository.VariantAttributeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductSpecificationRepository productSpecificationRepository;

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private VariantAttributeRepository variantAttributeRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category category;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        category = Category.builder()
                .id(10L)
                .name("Smartphone")
                .build();

        product1 = Product.builder()
                .id(101L)
                .brand("Apple")
                .series("iPhone")
                .model("16 Pro")
                .category(category)
                .description("Apple flagship smartphone")
                .imageUrl("https://example.com/iphone16pro.jpg")
                .hasVariants(true)
                .build();

        product2 = Product.builder()
                .id(102L)
                .brand("Samsung")
                .series("Galaxy")
                .model("S24 Ultra")
                .category(category)
                .description("Samsung flagship smartphone")
                .imageUrl("https://example.com/s24ultra.jpg")
                .hasVariants(false)
                .build();
    }

    @Test
    @DisplayName("Should return matching products on valid product search")
    void testSearchProducts_ValidQuery() {
        Page<Product> page = new PageImpl<>(List.of(product1), Pageable.ofSize(20), 1);
        when(productRepository.searchProducts(eq("iphone"), eq(""), any(Pageable.class))).thenReturn(page);

        ProductSearchResponse response = productService.searchProducts("iphone", "", 0, 20);

        assertThat(response).isNotNull();
        assertThat(response.getQuery()).isEqualTo("iphone");
        assertThat(response.getTotalResults()).isEqualTo(1);
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getProducts().get(0).getModel()).isEqualTo("16 Pro");
    }

    @Test
    @DisplayName("Should return matching products on partial / non-exact search")
    void testSearchProducts_PartialQuery() {
        Page<Product> page = new PageImpl<>(List.of(product1, product2), Pageable.ofSize(20), 2);
        when(productRepository.searchProducts(eq("phone"), eq(""), any(Pageable.class))).thenReturn(page);

        ProductSearchResponse response = productService.searchProducts("phone", null, 0, 20);

        assertThat(response.getTotalResults()).isEqualTo(2);
        assertThat(response.getProducts()).extracting("brand").contains("Apple", "Samsung");
    }

    @Test
    @DisplayName("Should return empty list when no products match query")
    void testSearchProducts_NoMatch() {
        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), Pageable.ofSize(20), 0);
        when(productRepository.searchProducts(eq("nonexistent"), eq(""), any(Pageable.class))).thenReturn(emptyPage);

        ProductSearchResponse response = productService.searchProducts("nonexistent", "", 0, 20);

        assertThat(response.getTotalResults()).isEqualTo(0);
        assertThat(response.getProducts()).isEmpty();
    }

    @Test
    @DisplayName("Should throw BadRequestException on invalid pagination arguments")
    void testSearchProducts_InvalidPagination() {
        assertThatThrownBy(() -> productService.searchProducts("test", "", -1, 20))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Page index");

        assertThatThrownBy(() -> productService.searchProducts("test", "", 0, 0))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Page size");
    }

    @Test
    @DisplayName("Should return product details for existing product")
    void testGetProductById_Existing() {
        ProductSpecification spec = ProductSpecification.builder()
                .id(1L)
                .product(product1)
                .attributeName("Display")
                .attributeValue("6.3 inch OLED")
                .displayOrder(1)
                .build();

        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
        when(productSpecificationRepository.findByProductIdOrderByDisplayOrderAsc(101L)).thenReturn(List.of(spec));

        ProductDetailResponse response = productService.getProductById(101L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getBrand()).isEqualTo("Apple");
        assertThat(response.getCategory().getName()).isEqualTo("Smartphone");
        assertThat(response.getSpecifications()).hasSize(1);
        assertThat(response.getSpecifications().get(0).getName()).isEqualTo("Display");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for missing product")
    void testGetProductById_Missing() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    @DisplayName("Should return list of variants for product with existing variants")
    void testGetProductVariants_ExistingVariants() {
        ProductVariant variant = ProductVariant.builder()
                .id(201L)
                .product(product1)
                .variantName("256GB / Natural Titanium")
                .isDefault(false)
                .build();

        VariantAttribute attr = VariantAttribute.builder()
                .id(301L)
                .variant(variant)
                .attributeName("Storage")
                .attributeValue("256GB")
                .build();

        when(productRepository.findById(101L)).thenReturn(Optional.of(product1));
        when(productVariantRepository.findByProductId(101L)).thenReturn(List.of(variant));
        when(variantAttributeRepository.findByVariantId(201L)).thenReturn(List.of(attr));

        ProductVariantListResponse response = productService.getProductVariants(101L);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(101L);
        assertThat(response.getVariants()).hasSize(1);
        assertThat(response.getVariants().get(0).getName()).isEqualTo("256GB / Natural Titanium");
        assertThat(response.getVariants().get(0).getAttributes().get(0).getValue()).isEqualTo("256GB");
    }

    @Test
    @DisplayName("Should return fallback Standard variant when product has no custom variants")
    void testGetProductVariants_StandardFallback() {
        when(productRepository.findById(102L)).thenReturn(Optional.of(product2));
        when(productVariantRepository.findByProductId(102L)).thenReturn(Collections.emptyList());

        ProductVariantListResponse response = productService.getProductVariants(102L);

        assertThat(response).isNotNull();
        assertThat(response.getProductId()).isEqualTo(102L);
        assertThat(response.getVariants()).hasSize(1);
        assertThat(response.getVariants().get(0).getName()).isEqualTo("Standard");
        assertThat(response.getVariants().get(0).getIsDefault()).isTrue();
    }
}
