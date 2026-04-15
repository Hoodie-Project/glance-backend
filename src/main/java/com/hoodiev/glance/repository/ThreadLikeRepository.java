package com.hoodiev.glance.repository;

import com.hoodiev.glance.domain.ThreadLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ThreadLikeRepository extends JpaRepository<ThreadLike, Long> {

    Optional<ThreadLike> findByThreadIdAndIpAddress(Long threadId, String ipAddress);
}
