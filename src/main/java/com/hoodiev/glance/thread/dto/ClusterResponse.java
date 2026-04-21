package com.hoodiev.glance.thread.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도 클러스터 (대표 좌표 + 포함 스레드 수)")
public record ClusterResponse(
        @Schema(description = "대표 위도 (그리드 내 평균)", example = "37.5563")
        double latitude,

        @Schema(description = "대표 경도 (그리드 내 평균)", example = "126.9236")
        double longitude,

        @Schema(description = "그리드 내 스레드 개수", example = "7")
        long count
) {}
