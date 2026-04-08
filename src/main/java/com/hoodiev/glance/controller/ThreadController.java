package com.hoodiev.glance.controller;

import com.hoodiev.glance.dto.common.DeleteRequest;
import com.hoodiev.glance.dto.thread.ThreadCreateRequest;
import com.hoodiev.glance.dto.thread.ThreadDetailResponse;
import com.hoodiev.glance.dto.thread.ThreadListResponse;
import com.hoodiev.glance.service.ThreadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/threads")
@RequiredArgsConstructor
public class ThreadController {

    private final ThreadService threadService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ThreadListResponse create(@Valid @RequestBody ThreadCreateRequest request) {
        return threadService.create(request);
    }

    @GetMapping
    public Page<ThreadListResponse> list(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radiusKm,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return threadService.getThreads(lat, lng, radiusKm, pageable);
    }

    @GetMapping("/{id}")
    public ThreadDetailResponse detail(@PathVariable Long id) {
        return threadService.getThread(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, @Valid @RequestBody DeleteRequest request) {
        threadService.delete(id, request.password());
    }
}
