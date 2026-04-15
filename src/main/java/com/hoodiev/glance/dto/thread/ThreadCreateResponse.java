package com.hoodiev.glance.dto.thread;

import com.hoodiev.glance.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "스레드 생성 응답")
public record ThreadCreateResponse(
        @Schema(description = "스레드 ID", example = "1")
        Long id,

        @Schema(description = "제목", example = "홍대 뭐 먹지")
        String title,

        @Schema(description = "본문", example = "오늘 홍대 놀러왔는데 추천 메뉴 있나요?")
        String content,

        @Schema(description = "위도", example = "37.5563")
        Double latitude,

        @Schema(description = "경도", example = "126.9236")
        Double longitude,

        @Schema(description = "역지오코딩된 장소명 (네이버 API)", example = "서울특별시 마포구 서교동", nullable = true)
        String locationName,

        @Schema(description = "힐끔 대상 성별", example = "FEMALE")
        Gender gender,

        @Schema(description = "태그 리스트", example = "[\"홍대\",\"맛집\"]")
        List<String> tags,

        @Schema(description = "좋아요 수", example = "0")
        Integer likeCount,

        @Schema(description = "댓글 수", example = "0")
        Integer commentCount,

        @Schema(description = "작성일시", example = "2026-04-15T18:30:00")
        LocalDateTime createdAt,

        @Schema(description = "서버가 생성한 랜덤 비밀번호. 요청 시 password를 직접 넣었다면 null.", example = "a7k2mQ4n", nullable = true)
        String generatedPassword
) {}
