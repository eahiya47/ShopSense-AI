package com.shopsense.repository;

import com.shopsense.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByBrandIgnoreCase(String brand);

    Page<Product> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrSeriesContainingIgnoreCase(
            String brand, String model, String series, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE " +
            "(:query IS NULL OR :query = '' OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.series) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.model) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.category.name) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:category IS NULL OR :category = '' OR " +
            "LOWER(p.category.name) = LOWER(:category))")
    Page<Product> searchProducts(@Param("query") String query, @Param("category") String category, Pageable pageable);
}
