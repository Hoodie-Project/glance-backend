package com.hoodiev.glance.thread.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지도 지역 마커 (지역명 + 게시글 수 + 마커 위치)")
public record RegionMarkerResponse(
        @Schema(description = "시도", example = "서울특별시")
        String sido,

        @Schema(description = "시군구", example = "마포구")
        String sigungu,

        @Schema(description = "동 (level=dong 일 때만 값 있음, sigungu 레벨에서는 null)", example = "서교동", nullable = true)
        String dong,

        @Schema(description = "해당 지역 게시글 수", example = "12")
        long count,

        @Schema(description = "마커 위치 위도 (해당 지역 게시글 평균)", example = "37.5507")
        double lat,

        @Schema(description = "마커 위치 경도 (해당 지역 게시글 평균)", example = "126.9236")
        double lng
) {}
