package com.hoodiev.glance.thread.service;

import com.hoodiev.glance.AbstractIntegrationTest;
import com.hoodiev.glance.comment.dto.CommentCreateRequest;
import com.hoodiev.glance.comment.service.CommentService;
import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.thread.dto.ThreadCreateRequest;
import com.hoodiev.glance.thread.dto.ThreadCreateResponse;
import com.hoodiev.glance.thread.entity.Gender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThreadServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ThreadService threadService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void Redis_초기화() {
        redisTemplate.execute((RedisCallback<Object>) conn -> {
            conn.serverCommands().flushAll();
            return null;
        });
    }

    @Test
    void 삭제된_스레드는_조회되지_않는다() {
        ThreadCreateResponse created = threadService.create(
                threadRequest("pw1234"),
                "10.0.0.1",
                "test-agent"
        );

        threadService.delete(created.id(), "pw1234");

        assertThatThrownBy(() -> threadService.getThread(created.id()))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void 삭제된_스레드에_댓글을_달_수_없다() {
        ThreadCreateResponse created = threadService.create(
                threadRequest("pw1234"),
                "10.0.0.2",
                "test-agent"
        );

        threadService.delete(created.id(), "pw1234");

        assertThatThrownBy(() -> commentService.create(
                created.id(),
                new CommentCreateRequest("댓글러", "댓글내용", null)
        )).isInstanceOf(EntityNotFoundException.class);
    }

    private static ThreadCreateRequest threadRequest(String password) {
        return new ThreadCreateRequest(
                "테스터", "테스트 제목", "테스트 내용",
                37.5563, 126.9236,
                Gender.FEMALE,
                password,
                null, null, null
        );
    }
}
