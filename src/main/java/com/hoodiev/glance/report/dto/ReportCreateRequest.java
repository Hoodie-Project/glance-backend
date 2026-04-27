package com.hoodiev.glance.report.dto;

import com.hoodiev.glance.report.entity.ReportReason;
import com.hoodiev.glance.report.entity.ReportTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 요청")
public record ReportCreateRequest(
        @Schema(description = "신고 대상 유형", requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"THREAD", "COMMENT"})
        @NotNull ReportTargetType targetType,

        @Schema(description = "신고 대상 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "42")
        @NotNull Long targetId,

        @Schema(description = "신고 사유", requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"SPAM", "PROFANITY", "ADULT_CONTENT", "PRIVACY_VIOLATION", "FRAUD", "OTHER"})
        @NotNull ReportReason reason,

        @Schema(description = "신고 부가 설명 (선택, 최대 200자)", example = "반복적으로 같은 광고를 올리고 있습니다.")
        @Size(max = 200) String description
) {}
