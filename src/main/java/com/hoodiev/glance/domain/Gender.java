package com.hoodiev.glance.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                힐끔 대상의 성별 (단일 선택).
                - `MALE`: 남성
                - `FEMALE`: 여성
                """,
        example = "FEMALE",
        allowableValues = {"MALE", "FEMALE"}
)
public enum Gender {
    MALE, FEMALE
}
