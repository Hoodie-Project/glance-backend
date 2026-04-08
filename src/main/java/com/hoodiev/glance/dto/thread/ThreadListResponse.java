package com.hoodiev.glance.dto.thread;

import java.time.LocalDateTime;
import java.util.List;

public record ThreadListResponse(
        Long id,
        String title,
        String content,
        Double latitude,
        Double longitude,
        String locationName,
        List<String> tags,
        Integer likeCount,
        Integer commentCount,
        LocalDateTime createdAt
) {}
