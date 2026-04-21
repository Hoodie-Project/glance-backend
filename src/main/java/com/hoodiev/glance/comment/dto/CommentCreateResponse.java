package com.hoodiev.glance.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 작성 응답")
public record CommentCreateResponse(
        @Schema(description = "댓글 ID", example = "10")
        Long id,

        @Schema(description = "댓글 내용", example = "저도 궁금해요")
        String content,

        @Schema(description = "좋아요 수", example = "0")
        Integer likeCount,

        @Schema(description = "작성일시", example = "2026-04-15T18:32:00")
        LocalDateTime createdAt,

        @Schema(description = "서버가 생성한 랜덤 비밀번호. 요청 시 password를 직접 넣었다면 null.", example = "a7k2mQ4n", nullable = true)
        String generatedPassword
) {}
