package com.shopsense.repository;

import com.shopsense.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductVariantId(Long productVariantId);

    List<Review> findByProductVariantIdAndPlatformId(Long productVariantId, Long platformId);

    void deleteByProductVariantIdAndPlatformId(Long productVariantId, Long platformId);

    void deleteByProductVariantId(Long productVariantId);
}
