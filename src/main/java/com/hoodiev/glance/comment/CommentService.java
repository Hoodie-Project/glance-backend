package com.hoodiev.glance.comment;

import com.hoodiev.glance.comment.dto.CommentCreateRequest;
import com.hoodiev.glance.comment.dto.CommentCreateResponse;
import com.hoodiev.glance.common.dto.LikeToggleResponse;
import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.common.exception.InvalidPasswordException;
import com.hoodiev.glance.common.util.PasswordGenerator;
import com.hoodiev.glance.thread.Thread;
import com.hoodiev.glance.thread.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ThreadRepository threadRepository;
    private final PasswordGenerator passwordGenerator;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public CommentCreateResponse create(Long threadId, CommentCreateRequest request) {
        Thread thread = threadRepository.findById(threadId)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Thread", threadId));

        boolean generated = request.password() == null || request.password().isBlank();
        String rawPassword = generated ? passwordGenerator.generate() : request.password();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Comment comment = Comment.builder()
                .thread(thread)
                .content(request.content())
                .password(encodedPassword)
                .build();

        Comment saved = commentRepository.save(comment);
        threadRepository.incrementCommentCount(threadId);

        return new CommentCreateResponse(
                saved.getId(),
                saved.getContent(),
                saved.getLikeCount() != null ? saved.getLikeCount() : 0,
                saved.getCreatedAt(),
                generated ? rawPassword : null);
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

        commentLikeRepository.deleteAllByCommentId(commentId);
        commentRepository.delete(comment);
        threadRepository.decrementCommentCount(threadId);
    }

    @Transactional
    public LikeToggleResponse toggleLike(Long threadId, Long commentId, String clientIp) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment", commentId));

        if (!comment.getThread().getId().equals(threadId)) {
            throw new EntityNotFoundException("Comment", commentId);
        }

        int current = comment.getLikeCount() != null ? comment.getLikeCount() : 0;

        Optional<CommentLike> existing = commentLikeRepository.findByCommentIdAndIpAddress(commentId, clientIp);
        boolean liked;
        int newCount;
        if (existing.isPresent()) {
            commentLikeRepository.delete(existing.get());
            commentRepository.decrementLikeCount(commentId);
            liked = false;
            newCount = current - 1;
        } else {
            commentLikeRepository.save(CommentLike.builder()
                    .commentId(commentId)
                    .ipAddress(clientIp)
                    .build());
            commentRepository.incrementLikeCount(commentId);
            liked = true;
            newCount = current + 1;
        }
        return new LikeToggleResponse(liked, newCount);
    }
}
