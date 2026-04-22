package com.hoodiev.glance.thread;

import com.hoodiev.glance.comment.CommentRepository;
import com.hoodiev.glance.comment.dto.CommentResponse;
import com.hoodiev.glance.common.dto.LikeToggleResponse;
import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.common.exception.InvalidPasswordException;
import com.hoodiev.glance.common.exception.RateLimitExceededException;
import com.hoodiev.glance.common.util.PasswordGenerator;
import com.hoodiev.glance.common.util.RateLimiter;
import com.hoodiev.glance.region.GeocodingService;
import com.hoodiev.glance.region.LocationInfo;
import com.hoodiev.glance.region.Region;
import com.hoodiev.glance.region.RegionRepository;
import com.hoodiev.glance.region.dto.RegionResponse;
import com.hoodiev.glance.thread.dto.ClusterResponse;
import com.hoodiev.glance.thread.dto.RangeFilter;
import com.hoodiev.glance.thread.dto.RegionMarkerResponse;
import com.hoodiev.glance.thread.dto.ThreadCreateRequest;
import com.hoodiev.glance.thread.dto.ThreadCreateResponse;
import com.hoodiev.glance.thread.dto.ThreadDetailResponse;
import com.hoodiev.glance.thread.dto.ThreadListResponse;
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
    private final ThreadLikeRepository threadLikeRepository;
    private final CommentRepository commentRepository;
    private final RegionRepository regionRepository;
    private final GeocodingService geocodingService;
    private final RateLimiter rateLimiter;
    private final PasswordGenerator passwordGenerator;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ThreadCreateResponse create(ThreadCreateRequest request, String clientIp) {
        if (!rateLimiter.tryAcquire(clientIp)) {
            throw new RateLimitExceededException();
        }

        LocationInfo location = geocodingService.reverseGeocode(request.latitude(), request.longitude());
        Region region = findOrCreateRegion(location);

        boolean generated = request.password() == null || request.password().isBlank();
        String rawPassword = generated ? passwordGenerator.generate() : request.password();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Thread thread = Thread.builder()
                .nickname(request.nickname())
                .title(request.title())
                .content(request.content())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .region(region)
                .password(encodedPassword)
                .gender(request.gender())
                .tags(request.tags())
                .animalLooks(request.animalLooks())
                .vibeStyles(request.vibeStyles())
                .build();

        Thread saved = threadRepository.save(thread);

        return new ThreadCreateResponse(
                saved.getId(), saved.getNickname(), saved.getTitle(), saved.getContent(),
                saved.getLatitude(), saved.getLongitude(),
                RegionResponse.from(saved.getRegion()),
                saved.getGender(), saved.getTags(), saved.getAnimalLooks(), saved.getVibeStyles(),
                saved.getLikeCount(), saved.getCommentCount(),
                saved.getCreatedAt(), generated ? rawPassword : null);
    }

    public Page<ThreadListResponse> getThreads(double lat, double lng, RangeFilter range,
                                               String tag, Gender gender, Pageable pageable) {
        String genderParam = gender == null ? null : gender.name();
        String tagParam = (tag == null || tag.isBlank()) ? null : tag;
        return threadRepository.searchThreads(lat, lng, range.getKm(), genderParam, tagParam, pageable)
                .map(this::toListResponse);
    }

    public Page<ThreadListResponse> searchByTag(String tag, Pageable pageable) {
        return threadRepository.searchByTag(tag, pageable).map(this::toListResponse);
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

    public List<RegionMarkerResponse> getRegionMarkers(String level, String sido, String sigungu) {
        if ("dong".equalsIgnoreCase(level)) {
            return threadRepository.findMarkersByDong(sido, sigungu).stream()
                    .map(row -> new RegionMarkerResponse(
                            (String) row[0],
                            (String) row[1],
                            (String) row[2],
                            ((Number) row[3]).longValue(),
                            ((Number) row[4]).doubleValue(),
                            ((Number) row[5]).doubleValue()))
                    .toList();
        }
        return threadRepository.findMarkersBySigungu(sido).stream()
                .map(row -> new RegionMarkerResponse(
                        (String) row[0],
                        (String) row[1],
                        null,
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).doubleValue(),
                        ((Number) row[5]).doubleValue()))
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
                thread.getId(), thread.getNickname(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(),
                RegionResponse.from(thread.getRegion()),
                thread.getGender(), thread.getTags(), thread.getAnimalLooks(), thread.getVibeStyles(),
                thread.getLikeCount(), thread.getCommentCount(),
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

    private Region findOrCreateRegion(LocationInfo location) {
        if (location == null) return null;
        return regionRepository.findByLegalCode(location.legalCode())
                .map(region -> {
                    if (!region.getSido().equals(location.sido())
                            || !region.getSigungu().equals(location.sigungu())
                            || !region.getDong().equals(location.dong())) {
                        region.updateNames(location.sido(), location.sigungu(), location.dong());
                    }
                    return region;
                })
                .orElseGet(() -> regionRepository.save(Region.builder()
                        .legalCode(location.legalCode())
                        .sido(location.sido())
                        .sigungu(location.sigungu())
                        .dong(location.dong())
                        .build()));
    }

    private ThreadListResponse toListResponse(Thread thread) {
        return new ThreadListResponse(
                thread.getId(), thread.getNickname(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(),
                RegionResponse.from(thread.getRegion()),
                thread.getGender(), thread.getTags(), thread.getAnimalLooks(), thread.getVibeStyles(),
                thread.getLikeCount(), thread.getCommentCount(),
                thread.getCreatedAt());
    }
}
