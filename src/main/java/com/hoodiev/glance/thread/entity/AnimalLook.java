package com.hoodiev.glance.thread.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        description = """
                힐끔 대상의 동물상 (다중 선택 가능).
                - `DOG`: 강아지상
                - `CAT`: 고양이상
                - `FOX`: 여우상
                - `RABBIT`: 토끼상
                - `DINOSAUR`: 공룡상
                - `DEER`: 사슴상
                - `WOLF`: 늑대상
                - `HAMSTER`: 햄스터상
                - `BEAR`: 곰돌이상
                """,
        example = "DOG",
        allowableValues = {"DOG", "CAT", "FOX", "RABBIT", "DINOSAUR", "DEER", "WOLF", "HAMSTER", "BEAR"}
)
public enum AnimalLook {
    DOG,
    CAT,
    FOX,
    RABBIT,
    DINOSAUR,
    DEER,
    WOLF,
    HAMSTER,
    BEAR
}
