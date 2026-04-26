package com.hoodiev.glance.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "HTTP 상태 코드", example = "404")
        int status,

        @Schema(description = "에러 코드", example = "ENTITY_NOT_FOUND")
        String code,

        @Schema(description = "에러 메시지", example = "스레드를 찾을 수 없습니다.")
        String message,

        @Schema(description = "발생 시각", example = "2026-04-15T18:30:00")
        LocalDateTime timestamp
) {}
