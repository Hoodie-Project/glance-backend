package com.hoodiev.glance.repository;

import com.hoodiev.glance.domain.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    Optional<CommentLike> findByCommentIdAndIpAddress(Long commentId, String ipAddress);

    @Modifying
    @Query("DELETE FROM CommentLike cl WHERE cl.commentId = :commentId")
    void deleteAllByCommentId(@Param("commentId") Long commentId);
}
