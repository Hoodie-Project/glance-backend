package com.hoodiev.glance.thread;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                힐끔 대상의 분위기 및 스타일 (다중 선택 가능).
                - `COLD_HANDSOME`: 냉미남 (차가운 느낌의 남성)
                - `COLD_BEAUTY`: 냉미녀 (차가운 느낌의 여성)
                - `WARM_HANDSOME`: 온미남 (따뜻한 느낌의 남성)
                - `WARM_BEAUTY`: 온미녀 (따뜻한 느낌의 여성)
                - `HARMLESS`: 무해함 (순하고 해맑은 인상)
                - `DECADENT`: 퇴폐미 (퇴폐적이고 섹시한 인상)
                - `CLASSIC_HANDSOME`: 정석미남 (전형적으로 잘생긴 외모)
                - `CLASSIC_BEAUTY`: 정석미녀 (전형적으로 예쁜 외모)
                - `FRESH`: 과즙상 (상큼발랄 싱그러운 인상)
                """,
        example = "COLD_HANDSOME",
        allowableValues = {
                "COLD_HANDSOME", "COLD_BEAUTY", "WARM_HANDSOME", "WARM_BEAUTY",
                "HARMLESS", "DECADENT", "CLASSIC_HANDSOME", "CLASSIC_BEAUTY", "FRESH"
        }
)
public enum VibeStyle {
    COLD_HANDSOME,
    COLD_BEAUTY,
    WARM_HANDSOME,
    WARM_BEAUTY,
    HARMLESS,
    DECADENT,
    CLASSIC_HANDSOME,
    CLASSIC_BEAUTY,
    FRESH
}
