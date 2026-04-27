package com.hoodiev.glance.thread.service;

import com.hoodiev.glance.comment.repository.CommentRepository;
import com.hoodiev.glance.comment.dto.CommentResponse;
import com.hoodiev.glance.common.dto.LikeToggleResponse;
import com.hoodiev.glance.common.exception.BoundingBoxTooLargeException;
import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.common.exception.InvalidPasswordException;
import com.hoodiev.glance.common.exception.RateLimitExceededException;
import com.hoodiev.glance.common.util.PasswordGenerator;
import com.hoodiev.glance.common.util.RateLimiter;
import com.hoodiev.glance.region.service.GeocodingService;
import com.hoodiev.glance.region.entity.LocationInfo;
import com.hoodiev.glance.region.entity.Region;
import com.hoodiev.glance.region.repository.RegionRepository;
import com.hoodiev.glance.region.dto.RegionResponse;
import com.hoodiev.glance.thread.entity.Gender;
import com.hoodiev.glance.thread.entity.Tag;
import com.hoodiev.glance.thread.entity.Thread;
import com.hoodiev.glance.thread.entity.ThreadLike;
import com.hoodiev.glance.thread.repository.TagRepository;
import com.hoodiev.glance.thread.repository.ThreadLikeRepository;
import com.hoodiev.glance.thread.repository.ThreadRepository;
import com.hoodiev.glance.thread.dto.DongMarkerResponse;
import com.hoodiev.glance.thread.dto.FeedResponse;
import com.hoodiev.glance.thread.dto.ThreadCreateRequest;
import com.hoodiev.glance.thread.dto.ThreadCreateResponse;
import com.hoodiev.glance.thread.dto.ThreadDetailResponse;
import com.hoodiev.glance.thread.dto.ThreadListResponse;
import com.hoodiev.glance.thread.dto.ThreadPinResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ThreadService {

    private static final Logger log = LoggerFactory.getLogger(ThreadService.class);

    private final ThreadRepository threadRepository;
    private final TagRepository tagRepository;
    private final ThreadLikeRepository threadLikeRepository;
    private final CommentRepository commentRepository;
    private final RegionRepository regionRepository;
    private final GeocodingService geocodingService;
    private final RateLimiter rateLimiter;
    private final PasswordGenerator passwordGenerator;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public ThreadCreateResponse create(ThreadCreateRequest request, String clientIp, String userAgent) {
        if (!rateLimiter.tryAcquire(clientIp)) {
            log.warn("Rate limit exceeded - ip={}", clientIp);
            throw new RateLimitExceededException();
        }

        LocationInfo location = geocodingService.reverseGeocode(request.latitude(), request.longitude());
        Region region = findOrCreateRegion(location);

        boolean generated = request.password() == null || request.password().isBlank();
        String rawPassword = generated ? passwordGenerator.generate() : request.password();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Set<Tag> tags = request.tags() == null ? new HashSet<>() :
                request.tags().stream().map(this::findOrCreateTag).collect(Collectors.toSet());

        Thread thread = Thread.builder()
                .nickname(request.nickname())
                .title(request.title())
                .content(request.content())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .region(region)
                .password(encodedPassword)
                .gender(request.gender())
                .tags(tags)
                .animalLooks(request.animalLooks())
                .vibeStyles(request.vibeStyles())
                .clientIp(clientIp)
                .userAgent(userAgent)
                .build();

        Thread saved = threadRepository.save(thread);
        log.info("Thread created - id={}, ip={}", saved.getId(), clientIp);

        return new ThreadCreateResponse(
                saved.getId(), saved.getNickname(), saved.getTitle(), saved.getContent(),
                saved.getLatitude(), saved.getLongitude(),
                RegionResponse.from(saved.getRegion()),
                saved.getGender(), tagNames(saved.getTags()), saved.getAnimalLooks(), saved.getVibeStyles(),
                saved.getLikeCount(), saved.getCommentCount(),
                saved.getCreatedAt(), generated ? rawPassword : null);
    }

    public FeedResponse getFeed(Long cursor, int size) {
        List<Thread> rows = threadRepository.findFeed(cursor, PageRequest.of(0, size + 1));
        boolean hasMore = rows.size() > size;
        List<Thread> threads = hasMore ? rows.subList(0, size) : rows;
        Long nextCursor = hasMore ? threads.get(threads.size() - 1).getId() : null;
        return new FeedResponse(threads.stream().map(this::toListResponse).toList(), nextCursor, hasMore);
    }

    public FeedResponse getNearbyFeed(double lat, double lng, double radiusKm, Long cursor, int size) {
        List<Thread> rows = threadRepository.findNearbyFeed(lat, lng, radiusKm, cursor, size + 1);
        boolean hasMore = rows.size() > size;
        List<Thread> threads = hasMore ? rows.subList(0, size) : rows;
        Long nextCursor = hasMore ? threads.get(threads.size() - 1).getId() : null;
        return new FeedResponse(threads.stream().map(this::toListResponse).toList(), nextCursor, hasMore);
    }

    private static final double MAX_PINS_SPAN = 0.072;    // ~8km
    private static final double DONG_MAX_HALF_SPAN = 0.1; // 중심 기준 ±0.1° (~11km), 약 100동

    public List<ThreadPinResponse> getPins(double swLat, double swLng, double neLat, double neLng, Gender gender) {
        if (neLat - swLat > MAX_PINS_SPAN || neLng - swLng > MAX_PINS_SPAN)
            throw new BoundingBoxTooLargeException(MAX_PINS_SPAN);
        String genderParam = (gender == null || gender == Gender.ALL) ? null : gender.name();
        return threadRepository.findPins(swLat, swLng, neLat, neLng, genderParam).stream()
                .map(row -> new ThreadPinResponse(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).doubleValue()))
                .toList();
    }

    public List<DongMarkerResponse> getDongMarkers(double swLat, double swLng, double neLat, double neLng) {
        double centerLat = (swLat + neLat) / 2;
        double centerLng = (swLng + neLng) / 2;
        double halfLat = Math.min((neLat - swLat) / 2, DONG_MAX_HALF_SPAN);
        double halfLng = Math.min((neLng - swLng) / 2, DONG_MAX_HALF_SPAN);
        double clampedSwLat = centerLat - halfLat;
        double clampedSwLng = centerLng - halfLng;
        double clampedNeLat = centerLat + halfLat;
        double clampedNeLng = centerLng + halfLng;
        return threadRepository.findDongMarkers(clampedSwLat, clampedSwLng, clampedNeLat, clampedNeLng).stream()
                .map(row -> new DongMarkerResponse(
                        (String) row[0],
                        (String) row[1],
                        (String) row[2],
                        ((Number) row[3]).longValue(),
                        ((Number) row[4]).doubleValue(),
                        ((Number) row[5]).doubleValue()))
                .toList();
    }

    public Page<ThreadListResponse> searchByTag(String tag, Pageable pageable) {
        String normalized = Normalizer.normalize(tag, Normalizer.Form.NFC)
                .replaceAll("^#+", "")
                .strip()
                .replaceAll("\\s+", " ")
                .toLowerCase();
        return threadRepository.searchByTag(normalized, pageable).map(this::toListResponse);
    }

    public ThreadDetailResponse getThread(Long id) {
        Thread thread = threadRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("스레드", id));

        List<CommentResponse> comments = commentRepository.findByThreadIdOrderByCreatedAtAsc(id)
                .stream()
                .map(comment -> new CommentResponse(
                        comment.getId(),
                        comment.getNickname(),
                        comment.getContent(),
                        comment.getLikeCount() != null ? comment.getLikeCount() : 0,
                        comment.getCreatedAt()))
                .toList();

        return new ThreadDetailResponse(
                thread.getId(), thread.getNickname(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(),
                RegionResponse.from(thread.getRegion()),
                thread.getGender(),
                tagNames(thread.getTags()),
                new HashSet<>(thread.getAnimalLooks()),
                new HashSet<>(thread.getVibeStyles()),
                thread.getLikeCount(), thread.getCommentCount(),
                thread.getCreatedAt(), comments);
    }

    @Transactional
    public LikeToggleResponse toggleLike(Long id, String clientIp) {
        Thread thread = threadRepository.findById(id)
                .filter(t -> t.getDeletedAt() == null)
                .orElseThrow(() -> new EntityNotFoundException("스레드", id));

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
                .orElseThrow(() -> new EntityNotFoundException("스레드", id));

        if (!passwordEncoder.matches(rawPassword, thread.getPassword())) {
            throw new InvalidPasswordException();
        }

        thread.softDelete();
        log.info("Thread deleted - id={}", id);
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
                .orElseGet(() -> {
                    String address = location.sido() + " " + location.sigungu() + " " + location.dong();
                    var center = geocodingService.geocode(address);
                    return regionRepository.save(Region.builder()
                            .legalCode(location.legalCode())
                            .sido(location.sido())
                            .sigungu(location.sigungu())
                            .dong(location.dong())
                            .centerLat(center != null ? center.getFirst() : null)
                            .centerLng(center != null ? center.getSecond() : null)
                            .build());
                });
    }

    private ThreadListResponse toListResponse(Thread thread) {
        return new ThreadListResponse(
                thread.getId(), thread.getNickname(), thread.getTitle(), thread.getContent(),
                thread.getLatitude(), thread.getLongitude(),
                RegionResponse.from(thread.getRegion()),
                thread.getGender(),
                tagNames(thread.getTags()),
                new HashSet<>(thread.getAnimalLooks()),
                new HashSet<>(thread.getVibeStyles()),
                thread.getLikeCount(), thread.getCommentCount(),
                thread.getCreatedAt());
    }

    private Tag findOrCreateTag(String name) {
        String normalized = Normalizer.normalize(name, Normalizer.Form.NFC)
                .replaceAll("^#+", "")
                .strip()
                .replaceAll("\\s+", " ")
                .toLowerCase();
        return tagRepository.findByName(normalized).orElseGet(() -> tagRepository.save(new Tag(normalized)));
    }

    private List<String> tagNames(Set<Tag> tags) {
        return tags.stream().map(Tag::getName).toList();
    }
}
