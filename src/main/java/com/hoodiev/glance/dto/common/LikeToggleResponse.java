package com.hoodiev.glance.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요 토글 결과")
public record LikeToggleResponse(
        @Schema(description = "요청 후 현재 사용자(IP) 좋아요 상태. true면 방금 좋아요, false면 방금 해제.", example = "true")
        boolean liked,

        @Schema(description = "갱신된 총 좋아요 수", example = "13")
        int likeCount
) {}
