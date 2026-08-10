package com.shopsense.repository;

import com.shopsense.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);

    Optional<ProductVariant> findByProductIdAndIsDefaultTrue(Long productId);
}
