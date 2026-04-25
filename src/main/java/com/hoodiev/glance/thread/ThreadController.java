package com.hoodiev.glance.thread;

import com.hoodiev.glance.common.dto.DeleteRequest;
import com.hoodiev.glance.common.dto.ErrorResponse;
import com.hoodiev.glance.common.dto.LikeToggleResponse;
import com.hoodiev.glance.thread.dto.ClusterResponse;
import com.hoodiev.glance.thread.dto.RangeFilter;
import com.hoodiev.glance.thread.dto.RegionMarkerResponse;
import com.hoodiev.glance.thread.dto.ThreadCreateRequest;
import com.hoodiev.glance.thread.dto.ThreadCreateResponse;
import com.hoodiev.glance.thread.dto.ThreadDetailResponse;
import com.hoodiev.glance.thread.dto.ThreadListResponse;
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
            summary = "스레드 목록 조회 (피드)",
            description = """
                    내 위치 기준 반경 내 스레드를 최신순으로 반환합니다. 무한 스크롤 피드용.

                    ### 거리 계산
                    - Haversine 공식으로 구면 거리 계산 (km 단위)
                    - `deleted_at IS NULL` 인 스레드만 노출

                    ### 반경(range) 필터 허용값
                    - `0.5`: 500m 이내
                    - `2`: 2km 이내
                    - `5`: 5km 이내
                    - `all`: 거리 무제한 (디폴트)

                    ### 선택 필터
                    - `tag`: 해당 문자열이 스레드 태그 리스트에 포함된 것만
                    - `gender`: `MALE` / `FEMALE` 중 하나로 필터

                    ### 페이지네이션
                    - `page`: 0-based
                    - `size`: 기본 10, 최대 권장 50
                    """)
    @GetMapping
    public Page<ThreadListResponse> list(
            @Parameter(description = "내 위치 위도", example = "37.5563", required = true)
            @RequestParam double lat,

            @Parameter(description = "내 위치 경도", example = "126.9236", required = true)
            @RequestParam double lng,

            @Parameter(
                    description = "반경 필터. 허용값: `0.5` (500m), `2` (2km), `5` (5km), `all` (무제한)",
                    example = "all",
                    schema = @Schema(allowableValues = {"0.5", "2", "5", "all"}, defaultValue = "all")
            )
            @RequestParam(defaultValue = "all") String range,

            @Parameter(description = "해시태그 정확 일치 필터 (선택)", example = "홍대")
            @RequestParam(required = false) String tag,

            @Parameter(
                    description = "힐끔 대상 성별 필터 (선택). MALE=남성, FEMALE=여성",
                    example = "FEMALE",
                    schema = @Schema(allowableValues = {"MALE", "FEMALE"})
            )
            @RequestParam(required = false) Gender gender,

            @Parameter(description = "페이지네이션 (page, size=10 고정 권장)")
            @PageableDefault(size = 10) Pageable pageable) {
        return threadService.getThreads(lat, lng, RangeFilter.from(range), tag, gender, pageable);
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
            summary = "지도 클러스터 조회",
            description = """
                    화면에 보이는 bounding box 영역의 스레드들을 `zoomLevel` 기반 그리드 셀로 묶어
                    [대표 좌표, 개수] 배열로 반환합니다. 클러스터 마커 렌더링용.

                    ### 그리드 크기 계산
                    `gridSize = 180 / 2^zoomLevel`
                    - zoom이 클수록(확대) 셀이 작아져 군집이 잘게 쪼개짐
                    - zoom 1~20 권장

                    ### bounding box
                    - `swLat`/`swLng`: 남서쪽 모서리
                    - `neLat`/`neLng`: 북동쪽 모서리
                    - 지도 라이브러리의 `getBounds()` 값을 그대로 전달

                    ### 대표 좌표
                    각 그리드 셀 안에 있는 스레드들의 위경도 평균값
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

    @Operation(
            summary = "지도 지역 마커 조회",
            description = """
                    지도 줌 레벨에 따라 시군구 또는 동 단위로 게시글 수와 마커 좌표를 반환합니다.

                    ### level 파라미터
                    - `sigungu`: 시군구 단위로 묶어서 반환 (지도 축소 상태)
                    - `dong`: 동 단위로 묶어서 반환 (지도 확대 상태)

                    ### 필터 파라미터
                    - `sido`: 특정 시도 내로 제한 (선택). 미입력 시 전국 반환
                    - `sigungu`: `level=dong` 일 때 특정 시군구 내로 제한 (선택)

                    ### 마커 좌표 (lat, lng)
                    해당 지역 내 게시글들의 위경도 평균값. 지도에 마커를 찍을 위치로 사용.

                    ### 사용 예시
                    - 지도 축소: `?level=sigungu&sido=서울특별시` → 마포구 5개, 강남구 12개 ...
                    - 지도 확대: `?level=dong&sido=서울특별시&sigungu=강남구` → 역삼동 3개, 삼성동 9개 ...
                    """)
    @GetMapping("/map/regions")
    public List<RegionMarkerResponse> regionMarkers(
            @Parameter(
                    description = "집계 단위. `sigungu` (시군구) 또는 `dong` (동)",
                    example = "sigungu",
                    schema = @Schema(allowableValues = {"sigungu", "dong"}, defaultValue = "sigungu")
            )
            @RequestParam(defaultValue = "sigungu") String level,

            @Parameter(description = "시도 필터 (선택). 미입력 시 전국", example = "서울특별시")
            @RequestParam(required = false) String sido,

            @Parameter(description = "시군구 필터 (선택, level=dong 일 때 유효)", example = "강남구")
            @RequestParam(required = false) String sigungu) {
        return threadService.getRegionMarkers(level, sido, sigungu);
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
