package com.hoodiev.glance.service;

import com.hoodiev.glance.domain.Comment;
import com.hoodiev.glance.domain.Thread;
import com.hoodiev.glance.dto.comment.CommentCreateRequest;
import com.hoodiev.glance.dto.comment.CommentResponse;
import com.hoodiev.glance.exception.EntityNotFoundException;
import com.hoodiev.glance.exception.InvalidPasswordException;
import com.hoodiev.glance.repository.CommentRepository;
import com.hoodiev.glance.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final ThreadRepository threadRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public CommentResponse create(Long threadId, CommentCreateRequest request) {
        Thread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new EntityNotFoundException("Thread", threadId));

        String encodedPassword = passwordEncoder.encode(request.password());

        Comment comment = Comment.builder()
                .thread(thread)
                .content(request.content())
                .password(encodedPassword)
                .build();

        Comment saved = commentRepository.save(comment);
        threadRepository.incrementCommentCount(threadId);

        return new CommentResponse(saved.getId(), saved.getContent(), saved.getCreatedAt());
    }

    @Transactional
    public void delete(Long threadId, Long commentId, String rawPassword) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", commentId));

        if (!comment.getThread().getId().equals(threadId)) {
            throw new EntityNotFoundException("Comment", commentId);
        }

        if (!passwordEncoder.matches(rawPassword, comment.getPassword())) {
            throw new InvalidPasswordException();
        }

        commentRepository.delete(comment);
        threadRepository.decrementCommentCount(threadId);
    }
}
