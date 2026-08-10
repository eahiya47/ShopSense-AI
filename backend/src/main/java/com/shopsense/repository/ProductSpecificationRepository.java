package com.shopsense.repository;

import com.shopsense.entity.ProductSpecification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductSpecificationRepository extends JpaRepository<ProductSpecification, Long> {
    List<ProductSpecification> findByProductIdOrderByDisplayOrderAsc(Long productId);

    void deleteByProductId(Long productId);
}
