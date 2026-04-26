package com.hoodiev.glance.admin.service;

import com.hoodiev.glance.comment.entity.Comment;
import com.hoodiev.glance.comment.repository.CommentLikeRepository;
import com.hoodiev.glance.comment.repository.CommentRepository;
import com.hoodiev.glance.region.entity.Region;
import com.hoodiev.glance.region.repository.RegionRepository;
import com.hoodiev.glance.thread.entity.Thread;
import com.hoodiev.glance.thread.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final RegionRepository regionRepository;

    public AdminStats getStats() {
        long totalThreads = threadRepository.count();
        long activeThreads = threadRepository.countByDeletedAtIsNull();
        long deletedThreads = totalThreads - activeThreads;
        long totalComments = commentRepository.count();
        long totalRegions = regionRepository.count();
        long todayThreads = threadRepository.countByCreatedAtBetween(
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay());
        return new AdminStats(totalThreads, activeThreads, deletedThreads, totalComments, totalRegions, todayThreads);
    }

    public Page<Thread> getThreads(String keyword, Boolean showDeleted, Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return showDeleted != null && showDeleted
                    ? threadRepository.findAllByTitleContainingIgnoreCase(keyword, pageable)
                    : threadRepository.findByTitleContainingIgnoreCaseAndDeletedAtIsNull(keyword, pageable);
        }
        return showDeleted != null && showDeleted
                ? threadRepository.findAll(pageable)
                : threadRepository.findByDeletedAtIsNull(pageable);
    }

    public Thread getThread(Long id) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found: " + id));
        thread.getTags().size();
        thread.getAnimalLooks().size();
        thread.getVibeStyles().size();
        if (thread.getRegion() != null) thread.getRegion().getSido();
        return thread;
    }

    public List<Comment> getCommentsByThread(Long threadId) {
        return commentRepository.findByThreadIdOrderByCreatedAtAsc(threadId);
    }

    public Page<Comment> getComments(Pageable pageable) {
        return commentRepository.findAll(pageable);
    }

    public List<Region> getRegions() {
        return regionRepository.findAll();
    }

    @Transactional
    public void forceDeleteThread(Long id) {
        commentLikeRepository.deleteAllByThreadId(id);
        commentRepository.deleteAllByThreadId(id);
        threadRepository.deleteById(id);
    }

    @Transactional
    public void restoreThread(Long id) {
        threadRepository.findById(id).ifPresent(thread -> thread.restore());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found: " + commentId));
        commentLikeRepository.deleteAllByCommentId(commentId);
        commentRepository.delete(comment);
        threadRepository.decrementCommentCount(comment.getThread().getId());
    }

    public record AdminStats(
            long totalThreads,
            long activeThreads,
            long deletedThreads,
            long totalComments,
            long totalRegions,
            long todayThreads
    ) {}
}
