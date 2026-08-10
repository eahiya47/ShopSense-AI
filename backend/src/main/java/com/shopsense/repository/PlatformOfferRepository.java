package com.shopsense.repository;

import com.shopsense.entity.PlatformOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformOfferRepository extends JpaRepository<PlatformOffer, Long> {
    List<PlatformOffer> findByProductVariantId(Long productVariantId);

    Optional<PlatformOffer> findByProductVariantIdAndPlatformId(Long productVariantId, Long platformId);

    List<PlatformOffer> findByProductVariantIdAndPlatformIsActiveTrue(Long productVariantId);
}
