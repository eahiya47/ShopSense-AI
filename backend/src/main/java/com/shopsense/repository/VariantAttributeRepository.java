package com.shopsense.repository;

import com.shopsense.entity.VariantAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantAttributeRepository extends JpaRepository<VariantAttribute, Long> {
    List<VariantAttribute> findByVariantId(Long variantId);

    Optional<VariantAttribute> findByVariantIdAndAttributeName(Long variantId, String attributeName);
}
