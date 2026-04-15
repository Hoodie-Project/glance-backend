package com.hoodiev.glance.controller;

import com.hoodiev.glance.domain.Gender;
import com.hoodiev.glance.dto.common.DeleteRequest;
import com.hoodiev.glance.dto.common.ErrorResponse;
import com.hoodiev.glance.dto.common.LikeToggleResponse;
import com.hoodiev.glance.dto.thread.ClusterResponse;
import com.hoodiev.glance.dto.thread.RangeFilter;
import com.hoodiev.glance.dto.thread.ThreadCreateRequest;
import com.hoodiev.glance.dto.thread.ThreadCreateResponse;
import com.hoodiev.glance.dto.thread.ThreadDetailResponse;
import com.hoodiev.glance.dto.thread.ThreadListResponse;
import com.hoodiev.glance.service.ThreadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Thread", description = "스레드 API")
@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreadService threadService;

    @Operation(
            summary = "스레드 등록",
            description = """
                    제목/본문/위치/성별을 받아 스레드를 생성합니다.
                    - password 미입력 시 서버가 8자 랜덤 생성 → 응답 `generatedPassword`로 반환
                    - 네이버 Reverse Geocoding으로 `locationName` 자동 채움
                    - 동일 IP에서 1분에 3건 초과 시 429 반환
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "IP 레이트 리밋 초과 (1분 3건)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThreadCreateResponse create(
            @Valid @RequestBody ThreadCreateRequest request,
            HttpServletRequest http) {
        return threadService.create(request, clientIp(http));
    }

    @Operation(
            summary = "스레드 목록 조회 (피드)",
            description = """
                    내 위치 기준 반경 필터로 조회. 최신순 정렬, 페이지당 10건 (무한 스크롤 대응).
                    - `range`: `0.5` / `2` / `5` / `all` (디폴트 `all`)
                    - `tag`: 특정 태그 필터
                    - `gender`: 힐끔 대상 성별 필터 (`MALE` / `FEMALE`)
                    """)
    @GetMapping
    public Page<ThreadListResponse> list(
            @Parameter(description = "내 위치 위도", example = "37.5563", required = true)
            @RequestParam double lat,

            @Parameter(description = "내 위치 경도", example = "126.9236", required = true)
            @RequestParam double lng,

            @Parameter(description = "반경 필터 (`0.5` / `2` / `5` / `all`)", example = "all")
            @RequestParam(defaultValue = "all") String range,

            @Parameter(description = "태그 필터", example = "맛집")
            @RequestParam(required = false) String tag,

            @Parameter(description = "힐끔 대상 성별 필터", example = "FEMALE")
            @RequestParam(required = false) Gender gender,

            @Parameter(description = "페이지네이션 (page, size=10 고정 권장)")
            @PageableDefault(size = 10) Pageable pageable) {
        return threadService.getThreads(lat, lng, RangeFilter.from(range), tag, gender, pageable);
    }

    @Operation(
            summary = "지도 클러스터 조회",
            description = """
                    bounding box(swLat/swLng ~ neLat/neLng) 내 스레드를 `zoomLevel` 기반 grid 셀로 묶어
                    [대표 좌표, 개수]로 반환. zoom이 높을수록(=확대) 셀 크기가 작아져 군집이 잘게 쪼개집니다.
                    grid size 공식: `180 / 2^zoomLevel`.
                    """)
    @GetMapping("/map")
    public List<ClusterResponse> map(
            @Parameter(description = "지도 줌 레벨 (1~20 권장)", example = "13", required = true)
            @RequestParam int zoomLevel,

            @Parameter(description = "bounding box 남서 위도", example = "37.54", required = true)
            @RequestParam double swLat,

            @Parameter(description = "bounding box 남서 경도", example = "126.90", required = true)
            @RequestParam double swLng,

            @Parameter(description = "bounding box 북동 위도", example = "37.58", required = true)
            @RequestParam double neLat,

            @Parameter(description = "bounding box 북동 경도", example = "126.95", required = true)
            @RequestParam double neLng) {
        return threadService.getClusters(zoomLevel, swLat, swLng, neLat, neLng);
    }

    @Operation(summary = "스레드 상세 조회", description = "댓글 목록 포함 (오래된 순). Soft-deleted 스레드는 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 삭제된 스레드",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ThreadDetailResponse detail(
            @Parameter(description = "스레드 ID", example = "1", required = true)
            @PathVariable Long id) {
        return threadService.getThread(id);
    }

    @Operation(
            summary = "스레드 좋아요 토글",
            description = """
                    동일 IP가 한 번 누르면 +1, 다시 누르면 -1.
                    응답의 `liked`는 요청 후 상태(true=방금 좋아요, false=방금 해제).
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 삭제된 스레드",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{id}/likes")
    public LikeToggleResponse like(
            @Parameter(description = "스레드 ID", example = "1", required = true)
            @PathVariable Long id,
            HttpServletRequest http) {
        return threadService.toggleLike(id, clientIp(http));
    }

    @Operation(
            summary = "스레드 삭제 (Soft)",
            description = "작성 시 설정/발급된 비밀번호 일치 시 soft delete 처리. 목록/상세/좋아요에서 제외됨.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않거나 이미 삭제된 스레드",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "스레드 ID", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody DeleteRequest request) {
        threadService.delete(id, request.password());
    }

    private String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }
}
