package com.hoodiev.glance.service;

import com.hoodiev.glance.domain.Thread;
import com.hoodiev.glance.dto.comment.CommentResponse;
import com.hoodiev.glance.dto.thread.ThreadCreateRequest;
import com.hoodiev.glance.dto.thread.ThreadDetailResponse;
import com.hoodiev.glance.dto.thread.ThreadListResponse;
import com.hoodiev.glance.exception.EntityNotFoundException;
import com.hoodiev.glance.exception.InvalidPasswordException;
import com.hoodiev.glance.repository.CommentRepository;
import com.hoodiev.glance.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final GeocodingService geocodingService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ThreadListResponse create(ThreadCreateRequest request) {
        String locationName = geocodingService.reverseGeocode(request.latitude(), request.longitude());
        String encodedPassword = passwordEncoder.encode(request.password());

        Thread thread = Thread.builder()
                .title(request.title())
                .content(request.content())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .locationName(locationName)
                .password(encodedPassword)
                .tags(request.tags())
                .build();

        return toListResponse(threadRepository.save(thread));
    }

    public Page<ThreadListResponse> getThreads(double lat, double lng, double radiusKm, Pageable pageable) {
        return threadRepository.findByLocationWithinRadius(lat, lng, radiusKm, pageable)
                .map(this::toListResponse);
    }

    public ThreadDetailResponse getThread(Long id) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Thread", id));

        List<CommentResponse> comments = commentRepository.findByThreadIdOrderByCreatedAtAsc(id)
                .stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        comment.getCreatedAt()))
                .toList();

        return new ThreadDetailResponse(
                thread.getId(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(), thread.getLocationName(),
                thread.getTags(), thread.getLikeCount(), thread.getCommentCount(),
                thread.getCreatedAt(), comments);
    }

    @Transactional
    public void delete(Long id, String rawPassword) {
        Thread thread = threadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Thread", id));

        if (!passwordEncoder.matches(rawPassword, thread.getPassword())) {
            throw new InvalidPasswordException();
        }

        commentRepository.deleteAllByThreadId(id);
        threadRepository.delete(thread);
    }

    private ThreadListResponse toListResponse(Thread thread) {
        return new ThreadListResponse(
                thread.getId(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(), thread.getLocationName(),
                thread.getTags(), thread.getLikeCount(), thread.getCommentCount(),
                thread.getCreatedAt());
    }
}
