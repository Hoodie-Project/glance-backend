package com.hoodiev.glance.dto.thread;

import com.hoodiev.glance.domain.AnimalLook;
import com.hoodiev.glance.domain.Gender;
import com.hoodiev.glance.domain.VibeStyle;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Schema(description = "스레드 생성 응답")
public record ThreadCreateResponse(
        @Schema(description = "스레드 ID", example = "1")
        Long id,

        @Schema(description = "작성자 닉네임", example = "멋쟁이토마토")
        String nickname,

        @Schema(description = "제목", example = "홍대 뭐 먹지")
        String title,

        @Schema(description = "본문", example = "오늘 홍대 놀러왔는데 추천 메뉴 있나요?")
        String content,

        @Schema(description = "위도", example = "37.5563")
        Double latitude,

        @Schema(description = "경도", example = "126.9236")
        Double longitude,

        @Schema(description = "지역 정보 (역지오코딩 실패 시 null)", nullable = true)
        RegionResponse region,

        @Schema(description = "힐끔 대상 성별 (MALE: 남성, FEMALE: 여성)", example = "FEMALE", allowableValues = {"MALE", "FEMALE"})
        Gender gender,

        @Schema(description = "해시태그 리스트 (각 태그는 #을 제외한 순수 문자열)", example = "[\"홍대\",\"카페\"]")
        List<String> tags,

        @Schema(
                description = """
                        동물상 (다중 선택).
                        DOG=강아지, CAT=고양이, FOX=여우, RABBIT=토끼, DINOSAUR=공룡,
                        DEER=사슴, WOLF=늑대, HAMSTER=햄스터, BEAR=곰돌이
                        """,
                example = "[\"DOG\",\"CAT\"]"
        )
        Set<AnimalLook> animalLooks,

        @Schema(
                description = """
                        분위기/스타일 (다중 선택).
                        COLD_HANDSOME=냉미남, COLD_BEAUTY=냉미녀, WARM_HANDSOME=온미남, WARM_BEAUTY=온미녀,
                        HARMLESS=무해함, DECADENT=퇴폐미, CLASSIC_HANDSOME=정석미남,
                        CLASSIC_BEAUTY=정석미녀, FRESH=과즙상
                        """,
                example = "[\"COLD_HANDSOME\",\"DECADENT\"]"
        )
        Set<VibeStyle> vibeStyles,

        @Schema(description = "좋아요 수", example = "0")
        Integer likeCount,

        @Schema(description = "댓글 수", example = "0")
        Integer commentCount,

        @Schema(description = "작성일시", example = "2026-04-15T18:30:00")
        LocalDateTime createdAt,

        @Schema(description = "서버가 생성한 랜덤 비밀번호. 요청 시 password를 직접 넣었다면 null.", example = "a7k2mQ4n", nullable = true)
        String generatedPassword
) {}
