package com.hoodiev.glance.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    @Modifying
    void deleteAllByThreadId(Long threadId);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = COALESCE(c.likeCount, 0) + 1 WHERE c.id = :id")
    void incrementLikeCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = COALESCE(c.likeCount, 0) - 1 WHERE c.id = :id")
    void decrementLikeCount(@Param("id") Long id);
}
