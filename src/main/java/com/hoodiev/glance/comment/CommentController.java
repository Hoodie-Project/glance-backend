package com.hoodiev.glance.comment;

import com.hoodiev.glance.comment.dto.CommentCreateRequest;
import com.hoodiev.glance.comment.dto.CommentCreateResponse;
import com.hoodiev.glance.common.dto.DeleteRequest;
import com.hoodiev.glance.common.dto.ErrorResponse;
import com.hoodiev.glance.common.dto.LikeToggleResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("/api/threads/{threadId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(
            summary = "댓글 작성",
            description = """
                    지정한 스레드(`threadId`)에 댓글을 작성합니다.

                    ### 필수 입력
                    - `content`: 300자 이내

                    ### 선택 입력
                    - `password`: 4~8자. 미입력 시 서버가 8자 랜덤 생성 → 응답의 `generatedPassword`로 반환.
                      **이 값을 못 받으면 댓글 삭제가 불가하니 클라이언트가 반드시 저장/표시해야 함**

                    ### 자동 처리
                    - 부모 스레드의 `commentCount += 1`
                    - Soft-deleted 스레드에는 댓글 작성 불가 (404)
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "작성 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "스레드 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentCreateResponse create(
            @Parameter(description = "스레드 ID", example = "1", required = true)
            @PathVariable Long threadId,
            @Valid @RequestBody CommentCreateRequest request) {
        return commentService.create(threadId, request);
    }

    @Operation(
            summary = "댓글 삭제 (Hard Delete)",
            description = """
                    요청 바디의 `password`가 작성 시 설정/발급된 비밀번호와 일치하면 댓글을 **완전 삭제** 합니다.

                    - 댓글에 달린 좋아요(`CommentLike`) row 도 함께 삭제
                    - 부모 스레드의 `commentCount -= 1`
                    - 비밀번호 불일치 시 403 반환
                    - 스레드는 Soft Delete 인 반면 댓글은 Hard Delete
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "비밀번호 불일치",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "댓글 또는 스레드 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @Parameter(description = "스레드 ID", example = "1", required = true)
            @PathVariable Long threadId,
            @Parameter(description = "댓글 ID", example = "10", required = true)
            @PathVariable Long commentId,
            @Valid @RequestBody DeleteRequest request) {
        commentService.delete(threadId, commentId, request.password());
    }

    @Operation(
            summary = "댓글 좋아요 토글",
            description = """
                    동일 IP 기준 토글:
                    - 처음 누르면 `CommentLike` row 생성 + `likeCount += 1`
                    - 같은 IP가 다시 누르면 row 삭제 + `likeCount -= 1`

                    응답의 `liked`는 요청 후 상태 (true=방금 좋아요, false=방금 해제), `likeCount`는 갱신된 총 좋아요 수.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토글 성공"),
            @ApiResponse(responseCode = "404", description = "댓글 또는 스레드 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{commentId}/likes")
    public LikeToggleResponse like(
            @Parameter(description = "스레드 ID", example = "1", required = true)
            @PathVariable Long threadId,
            @Parameter(description = "댓글 ID", example = "10", required = true)
            @PathVariable Long commentId,
            HttpServletRequest http) {
        return commentService.toggleLike(threadId, commentId, clientIp(http));
    }

    private String clientIp(HttpServletRequest http) {
        String forwarded = http.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return http.getRemoteAddr();
    }
}
