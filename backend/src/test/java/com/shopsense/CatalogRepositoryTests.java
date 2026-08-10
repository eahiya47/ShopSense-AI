package com.shopsense;

import com.shopsense.entity.*;
import com.shopsense.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CatalogRepositoryTests {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSpecificationRepository productSpecificationRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private VariantAttributeRepository variantAttributeRepository;

    @Autowired
    private PlatformRepository platformRepository;

    @Autowired
    private PlatformOfferRepository platformOfferRepository;

    @Test
    @DisplayName("Should save and retrieve full catalog domain graph and platform offers")
    void testCatalogDomainPersistence() {
        // 1. Create Category
        Category category = Category.builder()
                .name("Smartphones")
                .build();
        Category savedCategory = categoryRepository.save(category);
        assertThat(savedCategory.getId()).isNotNull();

        // 2. Create Product
        Product product = Product.builder()
                .brand("Apple")
                .series("iPhone")
                .model("16 Pro")
                .category(savedCategory)
                .description("Flagship smartphone")
                .hasVariants(true)
                .build();
        Product savedProduct = productRepository.save(product);
        assertThat(savedProduct.getId()).isNotNull();

        // 3. Create ProductSpecification
        ProductSpecification spec = ProductSpecification.builder()
                .product(savedProduct)
                .attributeName("Processor")
                .attributeValue("A18 Pro")
                .displayOrder(1)
                .build();
        ProductSpecification savedSpec = productSpecificationRepository.save(spec);
        assertThat(savedSpec.getId()).isNotNull();

        // 4. Create ProductVariant
        ProductVariant variant = ProductVariant.builder()
                .product(savedProduct)
                .variantName("256GB / Natural Titanium")
                .isDefault(false)
                .build();
        ProductVariant savedVariant = productVariantRepository.save(variant);
        assertThat(savedVariant.getId()).isNotNull();

        // 5. Create VariantAttribute
        VariantAttribute attr = VariantAttribute.builder()
                .variant(savedVariant)
                .attributeName("Storage")
                .attributeValue("256GB")
                .build();
        VariantAttribute savedAttr = variantAttributeRepository.save(attr);
        assertThat(savedAttr.getId()).isNotNull();

        // 6. Create Platform
        Platform platform = Platform.builder()
                .name("Amazon")
                .websiteUrl("https://www.amazon.in")
                .isActive(true)
                .build();
        Platform savedPlatform = platformRepository.save(platform);
        assertThat(savedPlatform.getId()).isNotNull();

        // 7. Create PlatformOffer
        PlatformOffer offer = PlatformOffer.builder()
                .productVariant(savedVariant)
                .platform(savedPlatform)
                .originalPrice(new BigDecimal("119999.00"))
                .currentPrice(new BigDecimal("114999.00"))
                .currency("INR")
                .sellerName("Amazon Retail")
                .availabilityStatus("IN_STOCK")
                .productUrl("https://www.amazon.in/dp/sample")
                .build();
        PlatformOffer savedOffer = platformOfferRepository.save(offer);
        assertThat(savedOffer.getId()).isNotNull();

        // Verify Unique Constraint & Querying
        Optional<PlatformOffer> queriedOffer = platformOfferRepository
                .findByProductVariantIdAndPlatformId(savedVariant.getId(), savedPlatform.getId());
        assertThat(queriedOffer).isPresent();
        assertThat(queriedOffer.get().getCurrentPrice()).isEqualByComparingTo(new BigDecimal("114999.00"));
    }
}
