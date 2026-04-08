package com.hoodiev.glance.repository;

import com.hoodiev.glance.domain.Thread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThreadRepository extends JpaRepository<Thread, Long> {

    @Query(value = """
            SELECT * FROM threads t
            WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude))
                * cos(radians(t.longitude) - radians(:lng))
                + sin(radians(:lat)) * sin(radians(t.latitude)))) <= :radiusKm
            ORDER BY t.created_at DESC
            """,
            countQuery = """
            SELECT count(*) FROM threads t
            WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(t.latitude))
                * cos(radians(t.longitude) - radians(:lng))
                + sin(radians(:lat)) * sin(radians(t.latitude)))) <= :radiusKm
            """,
            nativeQuery = true)
    Page<Thread> findByLocationWithinRadius(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radiusKm") double radiusKm,
            Pageable pageable);

    @Modifying
    @Query("UPDATE Thread t SET t.commentCount = t.commentCount + 1 WHERE t.id = :id")
    void incrementCommentCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Thread t SET t.commentCount = t.commentCount - 1 WHERE t.id = :id")
    void decrementCommentCount(@Param("id") Long id);
}
