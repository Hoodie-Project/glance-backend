package com.hoodiev.glance.dto.thread;

import com.hoodiev.glance.domain.Region;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지역 정보")
public record RegionResponse(
        @Schema(description = "지역 ID", example = "1")
        Long id,

        @Schema(description = "시도", example = "서울특별시")
        String sido,

        @Schema(description = "시군구", example = "마포구")
        String sigungu,

        @Schema(description = "동", example = "서교동")
        String dong
) {
    public static RegionResponse from(Region region) {
        if (region == null) return null;
        return new RegionResponse(region.getId(), region.getSido(), region.getSigungu(), region.getDong());
    }
}
