package com.hoodiev.glance.region;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findBySidoAndSigunguAndDong(String sido, String sigungu, String dong);
}
