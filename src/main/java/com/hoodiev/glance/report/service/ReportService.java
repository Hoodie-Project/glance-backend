package com.hoodiev.glance.report.service;

import com.hoodiev.glance.comment.repository.CommentRepository;
import com.hoodiev.glance.common.exception.AlreadyReportedException;
import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.report.dto.ReportCreateRequest;
import com.hoodiev.glance.report.entity.Report;
import com.hoodiev.glance.report.entity.ReportTargetType;
import com.hoodiev.glance.report.repository.ReportRepository;
import com.hoodiev.glance.thread.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void report(ReportCreateRequest request, String clientIp, String userAgent) {
        validateTarget(request.targetType(), request.targetId());

        if (reportRepository.existsByTargetTypeAndTargetIdAndClientIp(request.targetType(), request.targetId(), clientIp)) {
            throw new AlreadyReportedException();
        }

        reportRepository.save(Report.builder()
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .description(request.description())
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build());
    }

    private void validateTarget(ReportTargetType targetType, Long targetId) {
        if (targetType == ReportTargetType.THREAD) {
            threadRepository.findById(targetId)
                    .filter(t -> t.getDeletedAt() == null)
                    .orElseThrow(() -> new EntityNotFoundException("스레드", targetId));
        } else {
            commentRepository.findById(targetId)
                    .filter(c -> c.getDeletedAt() == null)
                    .orElseThrow(() -> new EntityNotFoundException("댓글", targetId));
        }
    }
}
