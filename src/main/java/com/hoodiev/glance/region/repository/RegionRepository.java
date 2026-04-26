package com.hoodiev.glance.region.repository;

import com.hoodiev.glance.region.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByLegalCode(String legalCode);
}
