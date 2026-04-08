package com.hoodiev.glance.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CommentCreateRequest(
        @NotBlank @Size(max = 300) String content,
        @NotBlank @Size(min = 4) String password
) {}
