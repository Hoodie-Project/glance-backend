package com.hoodiev.glance.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "삭제 요청 (비밀번호 검증)")
public record DeleteRequest(
        @Schema(description = "작성 시 설정/발급된 비밀번호", example = "a7k2mQ4n", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank String password
) {}
