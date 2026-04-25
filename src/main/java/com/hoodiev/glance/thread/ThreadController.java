package com.hoodiev.glance.thread;

import com.hoodiev.glance.common.dto.DeleteRequest;
import com.hoodiev.glance.common.dto.ErrorResponse;
import com.hoodiev.glance.common.dto.LikeToggleResponse;
import com.hoodiev.glance.thread.dto.DongMarkerResponse;
import com.hoodiev.glance.thread.dto.FeedResponse;
import com.hoodiev.glance.thread.dto.ThreadCreateRequest;
import com.hoodiev.glance.thread.dto.ThreadCreateResponse;
import com.hoodiev.glance.thread.dto.ThreadDetailResponse;
import com.hoodiev.glance.thread.dto.ThreadListResponse;
import com.hoodiev.glance.thread.dto.ThreadPinResponse;
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
            summary = "스레드(힐끔 글) 등록",
            description = """
                    작성자의 현재 위치와 힐끔 대상의 특징을 받아 스레드를 생성합니다.

                    ### 필수 입력
                    - `nickname`, `title`, `content`, `latitude`, `longitude`, `gender`

                    ### 선택 입력
                    - `password`: 미입력 시 서버가 8자 랜덤 생성 → 응답 `generatedPassword`로 1회 반환. **이 값을 못 받으면 스레드 삭제가 불가하니 클라이언트가 반드시 저장/표시해야 함**
                    - `tags`: 해시태그 배열 (최대 5개, 각 태그 `#` 제외)
                    - `animalLooks`: 동물상 다중 선택
                    - `vibeStyles`: 분위기/스타일 다중 선택

                    ### 자동 처리
                    - 네이버 Reverse Geocoding으로 `region` (sido/sigungu/dong) 자동 채움
                    - `likeCount`, `commentCount`는 0으로 초기화

                    ### 제한
                    - 동일 IP 기준 **1분당 3건 초과** 시 429 (Too Many Requests) 반환
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
        return threadService.create(request, clientIp(http), http.getHeader("User-Agent"));
    }

    @Operation(
            summary = "스레드 피드 (최신순 무한스크롤)",
            description = """
                    전체 스레드를 최신순으로 반환합니다. Cursor 기반 무한스크롤 피드용.

                    ### Cursor 페이지네이션
                    - 첫 요청: `cursor` 생략 → 최신 `size`개 반환
                    - 이후 요청: 이전 응답의 `nextCursor`를 `cursor`로 전달
                    - `hasMore: false`이면 마지막 페이지
                    """)
    @GetMapping("/feed")
    public FeedResponse feed(
            @Parameter(description = "마지막으로 받은 스레드 ID (첫 요청 시 생략)")
            @RequestParam(required = false) Long cursor,

            @Parameter(description = "페이지 크기 (기본 20)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return threadService.getFeed(cursor, size);
    }

    @Operation(
            summary = "내 주변 피드 (반경 무한스크롤)",
            description = """
                    내 위치 기준 반경 내 스레드를 최신순으로 반환합니다. Cursor 기반 무한스크롤용.

                    ### 반경(radius) 허용값
                    - `0.5`: 500m
                    - `2`: 2km (기본값)
                    - `5`: 5km

                    ### Cursor 페이지네이션
                    - 첫 요청: `cursor` 생략 → 최신 `size`개 반환
                    - 이후 요청: 이전 응답의 `nextCursor`를 `cursor`로 전달
                    - `hasMore: false`이면 마지막 페이지
                    """)
    @GetMapping("/feed/nearby")
    public FeedResponse nearbyFeed(
            @Parameter(description = "내 위치 위도", example = "37.5563", required = true)
            @RequestParam double lat,

            @Parameter(description = "내 위치 경도", example = "126.9236", required = true)
            @RequestParam double lng,

            @Parameter(
                    description = "반경 (km). 허용값: `0.5`, `2`, `5`",
                    example = "2",
                    schema = @Schema(allowableValues = {"0.5", "2", "5"}, defaultValue = "2")
            )
            @RequestParam(defaultValue = "2") double radius,

            @Parameter(description = "마지막으로 받은 스레드 ID (첫 요청 시 생략)")
            @RequestParam(required = false) Long cursor,

            @Parameter(description = "페이지 크기 (기본 20)", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        return threadService.getNearbyFeed(lat, lng, radius, cursor, size);
    }

    @Operation(
            summary = "해시태그로 스레드 검색",
            description = """
                    특정 태그가 달린 스레드를 최신순으로 조회합니다. **위치 필터 없음** (전역 검색).

                    ### 동작
                    - 정확 일치 검색 (대소문자 구분)
                    - 클라이언트는 `#` 기호를 **제외한 순수 문자열**로 전달 (예: "홍대")
                    - Soft-deleted 스레드는 결과에서 제외

                    ### 페이지네이션
                    - `page`: 0-based
                    - `size`: 기본 20
                    """)
    @GetMapping("/search")
    public Page<ThreadListResponse> searchByTag(
            @Parameter(description = "검색할 태그", example = "홍대", required = true)
            @RequestParam String tag,

            @Parameter(description = "페이지네이션 (page, size=20 기본)")
            @PageableDefault(size = 20) Pageable pageable) {
        return threadService.searchByTag(tag, pageable);
    }

    @Operation(
            summary = "지도 핀 조회 (줌인)",
            description = """
                    현재 지도 화면(bounding box) 안의 스레드 핀을 반환합니다. 최신순, 최대 200개.

                    ### bounding box
                    - `swLat`/`swLng`: 남서쪽 모서리
                    - `neLat`/`neLng`: 북동쪽 모서리
                    - 지도 라이브러리의 `getBounds()` 값을 그대로 전달

                    ### 범위 제한
                    - 위도/경도 각각 **0.072° 이하** (~8km). 초과 시 400 반환.
                    - 줌인 상태(핀 표시)에서만 호출할 것
                    """)
    @GetMapping("/map/pins")
    public List<ThreadPinResponse> pins(
            @Parameter(description = "bounding box 남서 위도", example = "37.54", required = true)
            @RequestParam double swLat,

            @Parameter(description = "bounding box 남서 경도", example = "126.90", required = true)
            @RequestParam double swLng,

            @Parameter(description = "bounding box 북동 위도", example = "37.58", required = true)
            @RequestParam double neLat,

            @Parameter(description = "bounding box 북동 경도", example = "126.95", required = true)
            @RequestParam double neLng,

            @Parameter(
                    description = "성별 필터 (선택). ALL=전체, MALE=남, FEMALE=녀. 생략 시 전체",
                    schema = @Schema(allowableValues = {"ALL", "MALE", "FEMALE"})
            )
            @RequestParam(required = false) Gender gender) {
        return threadService.getPins(swLat, swLng, neLat, neLng, gender);
    }

    @Operation(
            summary = "지도 동 마커 조회 (줌아웃)",
            description = """
                    현재 지도 화면(bounding box) 안의 스레드를 동 단위로 집계하여 반환합니다.
                    줌아웃 상태에서 동 이름과 게시글 수를 마커로 표시할 때 사용.

                    ### 마커 좌표 (lat, lng)
                    해당 동에 속한 스레드들의 위경도 평균값.

                    ### 범위 제한
                    - bbox가 넓어도 중심 기준 **±0.1°** (~11km)로 자동 클램핑.
                    - 게시글 많은 동 상위 100개 반환.
                    """)
    @GetMapping("/map/dong")
    public List<DongMarkerResponse> dongMarkers(
            @Parameter(description = "bounding box 남서 위도", example = "37.40", required = true)
            @RequestParam double swLat,

            @Parameter(description = "bounding box 남서 경도", example = "126.80", required = true)
            @RequestParam double swLng,

            @Parameter(description = "bounding box 북동 위도", example = "37.70", required = true)
            @RequestParam double neLat,

            @Parameter(description = "bounding box 북동 경도", example = "127.10", required = true)
            @RequestParam double neLng) {
        return threadService.getDongMarkers(swLat, swLng, neLat, neLng);
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
                    동일 IP 기준 토글 방식:
                    - 처음 누르면 `ThreadLike` row 생성 + `likeCount += 1`
                    - 같은 IP가 다시 누르면 row 삭제 + `likeCount -= 1`
                    - 다른 IP는 독립적으로 카운트

                    응답의 `liked`는 **요청 후 상태**:
                    - `true`: 방금 좋아요 눌림
                    - `false`: 방금 좋아요 해제됨

                    `likeCount`는 갱신된 총 좋아요 수.
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
            summary = "스레드 삭제 (Soft Delete)",
            description = """
                    요청 바디의 `password`가 작성 시 설정/발급된 비밀번호와 일치하면 soft delete 처리합니다.

                    - 실제 DB row는 유지되고 `deleted_at` 컬럼만 채워짐
                    - 이후 목록/상세/검색/좋아요 API 에서 전부 제외됨
                    - 비밀번호 불일치 시 403 반환
                    """)
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
