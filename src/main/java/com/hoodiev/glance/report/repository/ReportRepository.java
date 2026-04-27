package com.hoodiev.glance.report.repository;

import com.hoodiev.glance.report.entity.Report;
import com.hoodiev.glance.report.entity.ReportStatus;
import com.hoodiev.glance.report.entity.ReportTargetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByTargetTypeAndTargetIdAndClientIp(ReportTargetType targetType, Long targetId, String clientIp);

    long countByStatus(ReportStatus status);

    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    Page<Report> findByTargetTypeOrderByCreatedAtDesc(ReportTargetType targetType, Pageable pageable);

    Page<Report> findByStatusAndTargetTypeOrderByCreatedAtDesc(ReportStatus status, ReportTargetType targetType, Pageable pageable);
}
