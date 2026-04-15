package com.hoodiev.glance.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "댓글 작성 요청")
public record CommentCreateRequest(
        @Schema(description = "댓글 내용 (최대 300자)", example = "저도 궁금해요", maxLength = 300, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 300) String content,

        @Schema(description = "삭제용 비밀번호. 미입력 시 서버가 랜덤 생성하여 generatedPassword로 반환.", example = "ab12cd", minLength = 4, maxLength = 8, nullable = true)
        @Size(min = 4, max = 8) String password
) {}
