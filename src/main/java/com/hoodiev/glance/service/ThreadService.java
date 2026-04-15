package com.hoodiev.glance.service;

import com.hoodiev.glance.domain.Gender;
import com.hoodiev.glance.domain.Thread;
import com.hoodiev.glance.domain.ThreadLike;
import com.hoodiev.glance.dto.comment.CommentResponse;
import com.hoodiev.glance.dto.common.LikeToggleResponse;
import com.hoodiev.glance.dto.thread.ClusterResponse;
import com.hoodiev.glance.dto.thread.RangeFilter;
import com.hoodiev.glance.dto.thread.ThreadCreateRequest;
import com.hoodiev.glance.dto.thread.ThreadCreateResponse;
import com.hoodiev.glance.dto.thread.ThreadDetailResponse;
import com.hoodiev.glance.dto.thread.ThreadListResponse;
import com.hoodiev.glance.exception.EntityNotFoundException;
import com.hoodiev.glance.exception.InvalidPasswordException;
import com.hoodiev.glance.exception.RateLimitExceededException;
import com.hoodiev.glance.repository.CommentRepository;
import com.hoodiev.glance.repository.ThreadLikeRepository;
import com.hoodiev.glance.repository.ThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThreadService {

    private final ThreadRepository threadRepository;
    private final CommentRepository commentRepository;
    private final ThreadLikeRepository threadLikeRepository;
    private final GeocodingService geocodingService;
    private final RateLimiter rateLimiter;
    private final PasswordGenerator passwordGenerator;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ThreadCreateResponse create(ThreadCreateRequest request, String clientIp) {
        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new RateLimitExceededException();
        }

        String locationName = geocodingService.reverseGeocode(request.latitude(), request.longitude());

        boolean generated = request.password() == null || request.password().isBlank();
        String rawPassword = generated ? passwordGenerator.generate() : request.password();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Thread thread = Thread.builder()
                .title(request.title())
                .content(request.content())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .locationName(locationName)
                .password(encodedPassword)
                .gender(request.gender())
                .tags(request.tags())
                .build();

        Thread saved = threadRepository.save(thread);

        return new ThreadCreateResponse(
                saved.getId(), saved.getTitle(), saved.getContent(),
                saved.getLatitude(), saved.getLongitude(), saved.getLocationName(),
                saved.getGender(), saved.getTags(), saved.getLikeCount(), saved.getCommentCount(),
                saved.getCreatedAt(), generated ? rawPassword : null);
    }

    public Page<ThreadListResponse> getThreads(double lat, double lng, RangeFilter range,
                                               String tag, Gender gender, Pageable pageable) {
        String genderParam = gender == null ? null : gender.name();
        String tagParam = (tag == null || tag.isBlank()) ? null : tag;
        return threadRepository.searchThreads(lat, lng, range.getKm(), genderParam, tagParam, pageable)
                .map(this::toListResponse);
    }

    public List<ClusterResponse> getClusters(int zoomLevel, double swLat, double swLng,
                                             double neLat, double neLng) {
        double gridSize = 180.0 / Math.pow(2, Math.max(1, zoomLevel));
        return threadRepository.findClusters(swLat, swLng, neLat, neLng, gridSize).stream()
                .map(row -> new ClusterResponse(
                        ((Number) row[0]).doubleValue(),
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).longValue()))
                .toList();
    }

    public ThreadDetailResponse getThread(Long id) {
        Thread thread = threadRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Thread", id));

        List<CommentResponse> comments = commentRepository.findByThreadIdOrderByCreatedAtAsc(id)
                .stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getContent(),
                        comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                        comment.getCreatedAt()))
                .toList();

        return new ThreadDetailResponse(
                thread.getId(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(), thread.getLocationName(),
                thread.getGender(), thread.getTags(), thread.getLikeCount(), thread.getCommentCount(),
                thread.getCreatedAt(), comments);
    }

    @Transactional
    public LikeToggleResponse toggleLike(Long id, String clientIp) {
        Thread thread = threadRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Thread", id));

        Optional<ThreadLike> existing = threadLikeRepository.findByThreadIdAndIpAddress(id, clientIp);
        boolean liked;
        int newCount;
        if (existing.isPresent()) {
            threadLikeRepository.delete(existing.get());
            threadRepository.decrementLikeCount(id);
            liked = false;
            newCount = thread.getLikeCount() - 1;
        } else {
            threadLikeRepository.save(ThreadLike.builder()
                    .threadId(thread.getId())
                    .ipAddress(clientIp)
                    .build());
            threadRepository.incrementLikeCount(id);
            liked = true;
            newCount = thread.getLikeCount() + 1;
        }
        return new LikeToggleResponse(liked, newCount);
    }

    @Transactional
    public void delete(Long id, String rawPassword) {
        Thread thread = threadRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("Thread", id));

        if (!passwordEncoder.matches(rawPassword, thread.getPassword())) {
            throw new InvalidPasswordException();
        }

        thread.softDelete();
    }

    private ThreadListResponse toListResponse(Thread thread) {
        return new ThreadListResponse(
                thread.getId(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(), thread.getLocationName(),
                thread.getGender(), thread.getTags(), thread.getLikeCount(), thread.getCommentCount(),
                thread.getCreatedAt());
    }
}
