package com.hoodiev.glance.thread.dto;

import java.util.List;

public record FeedResponse(
        List<ThreadListResponse> threads,
        Long nextCursor,
        boolean hasMore
) {}
