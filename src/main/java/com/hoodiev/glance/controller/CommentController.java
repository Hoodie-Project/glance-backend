package com.hoodiev.glance.controller;

import com.hoodiev.glance.dto.comment.CommentCreateRequest;
import com.hoodiev.glance.dto.comment.CommentCreateResponse;
import com.hoodiev.glance.dto.common.DeleteRequest;
import com.hoodiev.glance.dto.common.ErrorResponse;
import com.hoodiev.glance.dto.common.LikeToggleResponse;
import com.hoodiev.glance.service.CommentService;
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
                    300자 이내. password 미입력 시 서버가 8자 랜덤 생성 → 응답 `generatedPassword`로 반환.
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

    @Operation(summary = "댓글 삭제", description = "비밀번호 일치 시 삭제. 관련 좋아요도 함께 제거.")
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
            description = "동일 IP가 한 번 누르면 +1, 다시 누르면 -1. 응답의 `liked`는 요청 후 상태.")
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
