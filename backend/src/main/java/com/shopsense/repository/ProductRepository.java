package com.shopsense.repository;

import com.shopsense.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId);

    List<Product> findByBrandIgnoreCase(String brand);

    Page<Product> findByBrandContainingIgnoreCaseOrModelContainingIgnoreCaseOrSeriesContainingIgnoreCase(
            String brand, String model, String series, Pageable pageable);
}
