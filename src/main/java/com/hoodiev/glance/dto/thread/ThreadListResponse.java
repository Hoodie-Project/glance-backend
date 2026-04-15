package com.hoodiev.glance.dto.thread;

import com.hoodiev.glance.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "스레드 피드 아이템")
public record ThreadListResponse(
        @Schema(description = "스레드 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "홍대 뭐 먹지")
        String title,

        @Schema(description = "본문", example = "오늘 홍대 놀러왔는데...")
        String content,

        @Schema(description = "위도", example = "37.5563")
        Double latitude,

        @Schema(description = "경도", example = "126.9236")
        Double longitude,

        @Schema(description = "장소명", example = "서울특별시 마포구 서교동", nullable = true)
        String locationName,

        @Schema(description = "힐끔 대상 성별", example = "FEMALE")
        Gender gender,

        @Schema(description = "태그 리스트", example = "[\"홍대\",\"맛집\"]")
        List<String> tags,

        @Schema(description = "좋아요 수", example = "12")
        Integer likeCount,

        @Schema(description = "댓글 수", example = "4")
        Integer commentCount,

        @Schema(description = "작성일시", example = "2026-04-15T18:30:00")
        LocalDateTime createdAt
) {}
