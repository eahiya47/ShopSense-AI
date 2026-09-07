package com.shopsense.repository;

import com.shopsense.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    List<PriceHistory> findByProductVariantIdOrderByRecordedAtAsc(Long productVariantId);

    List<PriceHistory> findByProductVariantIdAndPlatformIdOrderByRecordedAtAsc(Long productVariantId, Long platformId);

    List<PriceHistory> findByProductVariantIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long productVariantId, LocalDateTime startDate, LocalDateTime endDate);

    List<PriceHistory> findByProductVariantIdAndPlatformIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long productVariantId, Long platformId, LocalDateTime startDate, LocalDateTime endDate);

    Optional<PriceHistory> findTopByProductVariantIdAndPlatformIdOrderByRecordedAtDesc(Long productVariantId, Long platformId);

    void deleteByProductVariantId(Long productVariantId);
}
