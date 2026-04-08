package com.hoodiev.glance.dto.thread;

import com.hoodiev.glance.dto.comment.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;

public record ThreadDetailResponse(
        Long id,
        String title,
        String content,
        Double latitude,
        Double longitude,
        String locationName,
        List<String> tags,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {}
