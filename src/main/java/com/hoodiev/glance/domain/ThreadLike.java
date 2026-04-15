package com.hoodiev.glance.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "thread_likes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_thread_likes_thread_ip",
                columnNames = {"thread_id", "ip_address"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ThreadLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "thread_id", nullable = false)
    private Long threadId;

    @Column(name = "ip_address", nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public ThreadLike(Long threadId, String ipAddress) {
        this.threadId = threadId;
        this.ipAddress = ipAddress;
    }
}
