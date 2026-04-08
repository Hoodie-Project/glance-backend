package com.hoodiev.glance.controller;

import com.hoodiev.glance.dto.comment.CommentCreateRequest;
import com.hoodiev.glance.dto.comment.CommentResponse;
import com.hoodiev.glance.dto.common.DeleteRequest;
import com.hoodiev.glance.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/threads/{threadId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponse create(
            @PathVariable Long threadId,
            @Valid @RequestBody CommentCreateRequest request) {
        return commentService.create(threadId, request);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long threadId,
            @PathVariable Long commentId,
            @Valid @RequestBody DeleteRequest request) {
        commentService.delete(threadId, commentId, request.password());
    }
}
