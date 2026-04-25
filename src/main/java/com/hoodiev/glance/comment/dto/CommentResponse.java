package com.hoodiev.glance.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "댓글 응답")
public record CommentResponse(
        @Schema(description = "댓글 ID", example = "10")
        Long id,

        @Schema(description = "닉네임", example = "익명의 고양이")
        String nickname,

        @Schema(description = "댓글 내용", example = "저도 궁금해요")
        String content,

        @Schema(description = "좋아요 수", example = "3")
        Integer likeCount,

        @Schema(description = "작성일시", example = "2026-04-15T18:32:00")
        LocalDateTime createdAt
) {}
