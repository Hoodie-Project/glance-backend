package com.hoodiev.glance.thread.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                힐끔 대상의 성별 (단일 선택).
                - `ALL`: 전체
                - `MALE`: 남성
                - `FEMALE`: 여성
                """,
        example = "ALL",
        allowableValues = {"ALL", "MALE", "FEMALE"}
)
public enum Gender {
    ALL, MALE, FEMALE
}
