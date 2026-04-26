package com.hoodiev.glance.comment.entity;

import com.hoodiev.glance.thread.entity.Thread;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private Thread thread;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false, length = 300)
    private String content;

    @Column(nullable = false)
    private String password;

    @Column
    private Integer likeCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Comment(Thread thread, String nickname, String content, String password) {
        this.thread = thread;
        this.nickname = nickname;
        this.content = content;
        this.password = password;
    }
}
