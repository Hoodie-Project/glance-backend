package com.hoodiev.glance.common.util;

import com.hoodiev.glance.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void clearRedis() {
        redisTemplate.execute((RedisCallback<Object>) conn -> {
            conn.serverCommands().flushAll();
            return null;
        });
    }

    @Test
    void allowsThreeRequestsForSameKey() {
        assertThat(rateLimiter.tryAcquire("test-ip")).isTrue();
        assertThat(rateLimiter.tryAcquire("test-ip")).isTrue();
        assertThat(rateLimiter.tryAcquire("test-ip")).isTrue();
    }

    @Test
    void blocksFourthRequestForSameKey() {
        rateLimiter.tryAcquire("test-ip");
        rateLimiter.tryAcquire("test-ip");
        rateLimiter.tryAcquire("test-ip");

        assertThat(rateLimiter.tryAcquire("test-ip")).isFalse();
    }

    @Test
    void differentKeysDoNotShareLimit() {
        rateLimiter.tryAcquire("ip-a");
        rateLimiter.tryAcquire("ip-a");
        rateLimiter.tryAcquire("ip-a");

        assertThat(rateLimiter.tryAcquire("ip-b")).isTrue();
    }
}
