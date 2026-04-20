package com.hoodiev.glance.dto.thread;

import com.hoodiev.glance.domain.AnimalLook;
import com.hoodiev.glance.domain.Gender;
import com.hoodiev.glance.domain.VibeStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Set;

@Schema(description = "스레드(힐끔 글) 생성 요청 바디")
public record ThreadCreateRequest(
        @Schema(
                description = "작성자 닉네임. 필수. 최대 20자. 중복 허용 (고유 식별자가 아님).",
                example = "멋쟁이토마토",
                minLength = 1,
                maxLength = 20,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank @Size(max = 20) String nickname,

        @Schema(
                description = "제목. 필수. 최대 30자.",
                example = "홍대입구역 2번출구 근처에서 본 분",
                minLength = 1,
                maxLength = 30,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank @Size(max = 30) String title,

        @Schema(
                description = "본문 내용. 필수. 최대 500자.",
                example = "오후 3시쯤 카페 2층 창가에 앉아계셨던 분, 혹시 이 글 보시면 연락 주세요!",
                minLength = 1,
                maxLength = 500,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank @Size(max = 500) String content,

        @Schema(
                description = "작성자 현재 위치의 위도 (WGS84). 필수. 네이버 Reverse Geocoding으로 지역 정보 자동 채움.",
                example = "37.5563",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull Double latitude,

        @Schema(
                description = "작성자 현재 위치의 경도 (WGS84). 필수.",
                example = "126.9236",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull Double longitude,

        @Schema(
                description = """
                        힐끔 대상의 성별. 필수. 단일 선택.
                        - `MALE`: 남성
                        - `FEMALE`: 여성
                        """,
                example = "FEMALE",
                allowableValues = {"MALE", "FEMALE"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull Gender gender,

        @Schema(
                description = """
                        삭제용 비밀번호. 선택. 4~8자.
                        - 미입력 시 서버가 8자 랜덤 비번을 생성하여 응답의 `generatedPassword`로 반환
                        - 이 비번은 추후 스레드 삭제 시 검증에 사용
                        """,
                example = "ab12cd",
                minLength = 4,
                maxLength = 8,
                nullable = true
        )
        @Size(min = 4, max = 8) String password,

        @Schema(
                description = """
                        자유 입력 해시태그 리스트. 선택. 최대 5개. 각 태그는 빈 문자열 불가.
                        `#` 기호는 붙이지 말고 순수 문자열만 전송.
                        `GET /api/threads/search?tag=...` 로 태그 검색 가능.
                        """,
                example = "[\"홍대\",\"카페\",\"2번출구\"]",
                nullable = true
        )
        @Size(max = 5) List<@NotBlank String> tags,

        @Schema(
                description = """
                        힐끔 대상의 동물상 (다중 선택). 선택.
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
                example = "[\"DOG\",\"CAT\"]",
                nullable = true
        )
        Set<AnimalLook> animalLooks,

        @Schema(
                description = """
                        힐끔 대상의 분위기/스타일 (다중 선택). 선택.
                        - `COLD_HANDSOME`: 냉미남
                        - `COLD_BEAUTY`: 냉미녀
                        - `WARM_HANDSOME`: 온미남
                        - `WARM_BEAUTY`: 온미녀
                        - `HARMLESS`: 무해함
                        - `DECADENT`: 퇴폐미
                        - `CLASSIC_HANDSOME`: 정석미남
                        - `CLASSIC_BEAUTY`: 정석미녀
                        - `FRESH`: 과즙상
                        """,
                example = "[\"COLD_HANDSOME\",\"DECADENT\"]",
                nullable = true
        )
        Set<VibeStyle> vibeStyles
) {}
