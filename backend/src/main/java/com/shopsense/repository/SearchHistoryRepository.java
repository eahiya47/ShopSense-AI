package com.shopsense.repository;

import com.shopsense.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findByUserIdOrderBySearchedAtDesc(Long userId);

    Optional<SearchHistory> findByIdAndUserId(Long id, Long userId);

    void deleteByUserId(Long userId);
}
