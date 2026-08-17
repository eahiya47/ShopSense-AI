package com.shopsense.repository;

import com.shopsense.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductVariantId(Long productVariantId);

    List<Review> findByProductVariantIdAndPlatformId(Long productVariantId, Long platformId);

    void deleteByProductVariantIdAndPlatformId(Long productVariantId, Long platformId);

    void deleteByProductVariantId(Long productVariantId);

    @Query("SELECT MAX(r.fetchedAt) FROM Review r WHERE r.productVariant.id = :productVariantId")
    Optional<LocalDateTime> findLatestFetchedAtByProductVariantId(@Param("productVariantId") Long productVariantId);
}
