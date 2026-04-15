package com.hoodiev.glance.dto.thread;

import com.hoodiev.glance.domain.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "스레드 생성 요청")
public record ThreadCreateRequest(
        @Schema(description = "제목 (최대 30자)", example = "홍대 뭐 먹지", maxLength = 30, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 30) String title,

        @Schema(description = "본문 (최대 500자)", example = "오늘 홍대 놀러왔는데 추천 메뉴 있나요?", maxLength = 500, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 500) String content,

        @Schema(description = "위도", example = "37.5563", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Double latitude,

        @Schema(description = "경도", example = "126.9236", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Double longitude,

        @Schema(description = "힐끔 대상 성별", example = "FEMALE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Gender gender,

        @Schema(description = "삭제용 비밀번호. 미입력 시 서버가 랜덤 생성하여 응답의 generatedPassword로 반환.", example = "ab12cd", minLength = 4, maxLength = 8, nullable = true)
        @Size(min = 4, max = 8) String password,

        @Schema(description = "태그 리스트 (최대 5개)", example = "[\"홍대\",\"맛집\"]", nullable = true)
        @Size(max = 5) List<@NotBlank String> tags
) {}
