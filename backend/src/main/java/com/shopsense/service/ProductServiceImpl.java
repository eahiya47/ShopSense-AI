package com.shopsense.service;

import com.shopsense.dto.*;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSpecificationRepository productSpecificationRepository;
    private final ProductVariantRepository productVariantRepository;
    private final VariantAttributeRepository variantAttributeRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductSearchResponse searchProducts(String query, String category, int page, int size) {
        if (page < 0) {
            throw new BadRequestException("Page index must not be less than zero");
        }
        if (size <= 0) {
            throw new BadRequestException("Page size must be greater than zero");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("brand").ascending().and(Sort.by("model").ascending()));
        String cleanQuery = (query != null) ? query.trim() : "";
        String cleanCategory = (category != null) ? category.trim() : "";

        Page<Product> productPage = productRepository.searchProducts(cleanQuery, cleanCategory, pageable);

        List<ProductSummaryResponse> productSummaries = productPage.getContent().stream()
                .map(this::mapToProductSummary)
                .collect(Collectors.toList());

        return ProductSearchResponse.builder()
                .query(cleanQuery)
                .page(productPage.getNumber())
                .size(productPage.getSize())
                .totalResults(productPage.getTotalElements())
                .totalPages(productPage.getTotalPages())
                .products(productSummaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<ProductSpecification> specs = productSpecificationRepository
                .findByProductIdOrderByDisplayOrderAsc(productId);

        List<ProductSpecificationResponse> specResponses = specs.stream()
                .map(s -> ProductSpecificationResponse.builder()
                        .name(s.getAttributeName())
                        .value(s.getAttributeValue())
                        .build())
                .collect(Collectors.toList());

        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(product.getCategory().getId())
                .name(product.getCategory().getName())
                .build();

        return ProductDetailResponse.builder()
                .id(product.getId())
                .brand(product.getBrand())
                .series(product.getSeries())
                .model(product.getModel())
                .category(categoryResponse)
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .hasVariants(product.getHasVariants())
                .specifications(specResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductVariantListResponse getProductVariants(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        List<ProductVariant> variants = productVariantRepository.findByProductId(product.getId());

        List<ProductVariantResponse> variantResponses;
        if (variants.isEmpty()) {
            // Return fallback Standard variant response if no explicit variants exist in
            // database
            variantResponses = Collections.singletonList(
                    ProductVariantResponse.builder()
                            .id(null)
                            .name("Standard")
                            .isDefault(true)
                            .attributes(Collections.emptyList())
                            .build());
        } else {
            variantResponses = variants.stream()
                    .map(this::mapToVariantResponse)
                    .collect(Collectors.toList());
        }

        return ProductVariantListResponse.builder()
                .productId(product.getId())
                .variants(variantResponses)
                .build();
    }

    private ProductSummaryResponse mapToProductSummary(Product product) {
        return ProductSummaryResponse.builder()
                .id(product.getId())
                .brand(product.getBrand())
                .series(product.getSeries())
                .model(product.getModel())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .imageUrl(product.getImageUrl())
                .hasVariants(product.getHasVariants())
                .build();
    }

    private ProductVariantResponse mapToVariantResponse(ProductVariant variant) {
        List<VariantAttribute> attrs = variantAttributeRepository.findByVariantId(variant.getId());
        List<VariantAttributeResponse> attrResponses = attrs.stream()
                .map(a -> VariantAttributeResponse.builder()
                        .name(a.getAttributeName())
                        .value(a.getAttributeValue())
                        .build())
                .collect(Collectors.toList());

        return ProductVariantResponse.builder()
                .id(variant.getId())
                .name(variant.getVariantName())
                .isDefault(variant.getIsDefault())
                .attributes(attrResponses)
                .build();
    }
}
