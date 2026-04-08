package com.hoodiev.glance.repository;

import com.hoodiev.glance.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByThreadIdOrderByCreatedAtAsc(Long threadId);

    @Modifying
    void deleteAllByThreadId(Long threadId);
}
