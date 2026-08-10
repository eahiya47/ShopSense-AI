package com.shopsense.repository;

import com.shopsense.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {
    Optional<Platform> findByNameIgnoreCase(String name);

    List<Platform> findByIsActiveTrue();
}
