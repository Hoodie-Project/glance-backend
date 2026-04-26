package com.hoodiev.glance.thread.repository;

import com.hoodiev.glance.thread.entity.Thread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThreadRepository extends JpaRepository<Thread, Long> {

    @Query("""
            SELECT t FROM Thread t
            WHERE t.deletedAt IS NULL
            AND (:cursor IS NULL OR t.id < :cursor)
            ORDER BY t.id DESC
            """)
    List<Thread> findFeed(@Param("cursor") Long cursor, Pageable pageable);

    @Query(value = """
            SELECT * FROM threads t
            WHERE t.deleted_at IS NULL
              AND (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude))
                  * cos(radians(t.longitude) - radians(:lng))
                  + sin(radians(:lat)) * sin(radians(t.latitude)))) <= :radiusKm
              AND (CAST(:cursor AS BIGINT) IS NULL OR t.id < :cursor)
            ORDER BY t.id DESC
            LIMIT :size
            """, nativeQuery = true)
    List<Thread> findNearbyFeed(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("cursor") Long cursor,
            @Param("size") int size);

    @Query(value = """
            SELECT t.id, t.latitude, t.longitude
            FROM threads t
            WHERE t.deleted_at IS NULL
              AND t.latitude BETWEEN :swLat AND :neLat
              AND t.longitude BETWEEN :swLng AND :neLng
              AND (CAST(:gender AS VARCHAR) IS NULL OR t.gender = :gender)
            ORDER BY t.created_at DESC
            LIMIT 200
            """, nativeQuery = true)
    List<Object[]> findPins(
            @Param("swLat") double swLat,
            @Param("swLng") double swLng,
            @Param("neLat") double neLat,
            @Param("neLng") double neLng,
            @Param("gender") String gender);

    @Query(value = """
            SELECT r.sido, r.sigungu, r.dong,
                   COUNT(t.id) as cnt, r.center_lat as lat, r.center_lng as lng
            FROM regions r
            JOIN threads t ON t.region_id = r.id AND t.deleted_at IS NULL
            WHERE r.center_lat IS NOT NULL
              AND r.center_lat BETWEEN :swLat AND :neLat
              AND r.center_lng BETWEEN :swLng AND :neLng
            GROUP BY r.id, r.sido, r.sigungu, r.dong, r.center_lat, r.center_lng
            ORDER BY cnt DESC
            LIMIT 100
            """, nativeQuery = true)
    List<Object[]> findDongMarkers(
            @Param("swLat") double swLat,
            @Param("swLng") double swLng,
            @Param("neLat") double neLat,
            @Param("neLng") double neLng);

    @Query("""
            SELECT DISTINCT t FROM Thread t
            JOIN t.tags tag
            WHERE tag = :tag
              AND t.deletedAt IS NULL
            ORDER BY t.createdAt DESC
            """)
    Page<Thread> searchByTag(@Param("tag") String tag, Pageable pageable);

    long countByDeletedAtIsNull();

    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    @EntityGraph(attributePaths = "region")
    Page<Thread> findByDeletedAtIsNull(Pageable pageable);

    @EntityGraph(attributePaths = "region")
    @Override
    Page<Thread> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "region")
    Page<Thread> findAllByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @EntityGraph(attributePaths = "region")
    Page<Thread> findByTitleContainingIgnoreCaseAndDeletedAtIsNull(String keyword, Pageable pageable);

    @Modifying
    @Query("UPDATE Thread t SET t.commentCount = t.commentCount + 1 WHERE t.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Thread t SET t.commentCount = t.commentCount - 1 WHERE t.id = :id")
    void decrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Thread t SET t.likeCount = t.likeCount + 1 WHERE t.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Thread t SET t.likeCount = t.likeCount - 1 WHERE t.id = :id")
    void decrementLikeCount(@Param("id") Long id);
}
