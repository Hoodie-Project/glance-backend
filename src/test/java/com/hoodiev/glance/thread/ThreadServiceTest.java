package com.hoodiev.glance.thread;

import com.hoodiev.glance.comment.CommentRepository;
import com.hoodiev.glance.common.exception.InvalidPasswordException;
import com.hoodiev.glance.common.exception.RateLimitExceededException;
import com.hoodiev.glance.common.util.PasswordGenerator;
import com.hoodiev.glance.common.util.RateLimiter;
import com.hoodiev.glance.region.GeocodingService;
import com.hoodiev.glance.region.RegionRepository;
import com.hoodiev.glance.thread.dto.ThreadCreateRequest;
import com.hoodiev.glance.thread.dto.ThreadCreateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ThreadServiceTest {

    @InjectMocks
    ThreadService threadService;

    @Mock ThreadRepository threadRepository;
    @Mock ThreadLikeRepository threadLikeRepository;
    @Mock CommentRepository commentRepository;
    @Mock RegionRepository regionRepository;
    @Mock GeocodingService geocodingService;
    @Mock RateLimiter rateLimiter;
    @Mock PasswordGenerator passwordGenerator;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @Test
    void create_성공_비밀번호_자동생성() {
        given(rateLimiter.tryAcquire("127.0.0.1")).willReturn(true);
        given(geocodingService.reverseGeocode(any(Double.class), any(Double.class))).willReturn(null);
        given(passwordGenerator.generate()).willReturn("auto1234");
        given(passwordEncoder.encode("auto1234")).willReturn("encoded");

        Thread saved = Thread.builder()
                .nickname("테스터").title("제목").content("내용")
                .latitude(37.5).longitude(127.0).gender(Gender.MALE)
                .password("encoded").build();
        given(threadRepository.save(any())).willReturn(saved);

        ThreadCreateRequest request = new ThreadCreateRequest(
                "테스터", "제목", "내용", 37.5, 127.0, Gender.MALE,
                null, null, null, null);

        ThreadCreateResponse response = threadService.create(request, "127.0.0.1");

        assertThat(response.generatedPassword()).isEqualTo("auto1234");
    }

    @Test
    void create_실패_레이트리밋초과() {
        given(rateLimiter.tryAcquire("127.0.0.1")).willReturn(false);

        ThreadCreateRequest request = new ThreadCreateRequest(
                "테스터", "제목", "내용", 37.5, 127.0, Gender.MALE,
                null, null, null, null);

        assertThatThrownBy(() -> threadService.create(request, "127.0.0.1"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void delete_실패_비밀번호불일치() {
        Thread thread = Thread.builder()
                .nickname("테스터").title("제목").content("내용")
                .latitude(37.5).longitude(127.0).gender(Gender.MALE)
                .password("encoded").build();

        given(threadRepository.findById(1L)).willReturn(Optional.of(thread));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> threadService.delete(1L, "wrong"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void toggleLike_좋아요_추가() {
        Thread thread = Thread.builder()
                .nickname("테스터").title("제목").content("내용")
                .latitude(37.5).longitude(127.0).gender(Gender.MALE)
                .password("encoded").build();

        given(threadRepository.findById(1L)).willReturn(Optional.of(thread));
        given(threadLikeRepository.findByThreadIdAndIpAddress(1L, "127.0.0.1")).willReturn(Optional.empty());

        var result = threadService.toggleLike(1L, "127.0.0.1");

        assertThat(result.liked()).isTrue();
    }

    @Test
    void toggleLike_좋아요_취소() {
        Thread thread = Thread.builder()
                .nickname("테스터").title("제목").content("내용")
                .latitude(37.5).longitude(127.0).gender(Gender.MALE)
                .password("encoded").build();

        ThreadLike like = ThreadLike.builder().threadId(1L).ipAddress("127.0.0.1").build();

        given(threadRepository.findById(1L)).willReturn(Optional.of(thread));
        given(threadLikeRepository.findByThreadIdAndIpAddress(1L, "127.0.0.1")).willReturn(Optional.of(like));

        var result = threadService.toggleLike(1L, "127.0.0.1");

        assertThat(result.liked()).isFalse();
    }
}
