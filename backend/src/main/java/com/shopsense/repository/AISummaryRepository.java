package com.shopsense.repository;

import com.shopsense.entity.AISummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AISummaryRepository extends JpaRepository<AISummary, Long> {
    Optional<AISummary> findByProductVariantId(Long productVariantId);
}
