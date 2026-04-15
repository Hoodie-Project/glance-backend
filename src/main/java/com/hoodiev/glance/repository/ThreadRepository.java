package com.hoodiev.glance.repository;

import com.hoodiev.glance.domain.Thread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThreadRepository extends JpaRepository<Thread, Long> {

    @Query(value = """
            SELECT * FROM threads t
            WHERE t.deleted_at IS NULL
              AND (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude))
                  * cos(radians(t.longitude) - radians(:lng))
                  + sin(radians(:lat)) * sin(radians(t.latitude)))) <= :radiusKm
              AND (CAST(:gender AS VARCHAR) IS NULL OR t.gender = :gender)
              AND (CAST(:tag AS VARCHAR) IS NULL OR EXISTS (
                  SELECT 1 FROM thread_tags tt
                  WHERE tt.thread_id = t.id AND tt.tag = :tag))
            ORDER BY t.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM threads t
            WHERE t.deleted_at IS NULL
              AND (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude))
                  * cos(radians(t.longitude) - radians(:lng))
                  + sin(radians(:lat)) * sin(radians(t.latitude)))) <= :radiusKm
              AND (CAST(:gender AS VARCHAR) IS NULL OR t.gender = :gender)
              AND (CAST(:tag AS VARCHAR) IS NULL OR EXISTS (
                  SELECT 1 FROM thread_tags tt
                  WHERE tt.thread_id = t.id AND tt.tag = :tag))
            """,
            nativeQuery = true)
    Page<Thread> searchThreads(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            @Param("gender") String gender,
            @Param("tag") String tag,
            Pageable pageable);

    @Query(value = """
            SELECT AVG(t.latitude) AS lat,
                   AVG(t.longitude) AS lng,
                   COUNT(*) AS cnt
            FROM threads t
            WHERE t.deleted_at IS NULL
              AND t.latitude BETWEEN :swLat AND :neLat
              AND t.longitude BETWEEN :swLng AND :neLng
            GROUP BY FLOOR(t.latitude / :gridSize), FLOOR(t.longitude / :gridSize)
            """,
            nativeQuery = true)
    List<Object[]> findClusters(
            @Param("swLat") double swLat,
            @Param("swLng") double swLng,
            @Param("neLat") double neLat,
            @Param("neLng") double neLng,
            @Param("gridSize") double gridSize);

    @Query("""
            SELECT DISTINCT t FROM Thread t
            JOIN t.tags tag
            WHERE tag = :tag
              AND t.deletedAt IS NULL
            ORDER BY t.createdAt DESC
            """)
    Page<Thread> searchByTag(@Param("tag") String tag, Pageable pageable);

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
