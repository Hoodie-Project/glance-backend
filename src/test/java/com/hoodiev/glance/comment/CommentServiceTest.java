package com.hoodiev.glance.comment;

import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.common.exception.InvalidPasswordException;
import com.hoodiev.glance.common.util.PasswordGenerator;
import com.hoodiev.glance.thread.Thread;
import com.hoodiev.glance.thread.Gender;
import com.hoodiev.glance.thread.ThreadRepository;
import com.hoodiev.glance.comment.dto.CommentCreateRequest;
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
class CommentServiceTest {

    @InjectMocks
    CommentService commentService;

    @Mock CommentRepository commentRepository;
    @Mock CommentLikeRepository commentLikeRepository;
    @Mock ThreadRepository threadRepository;
    @Mock PasswordGenerator passwordGenerator;
    @Mock BCryptPasswordEncoder passwordEncoder;

    @Test
    void create_성공_비밀번호_자동생성() {
        Thread thread = Thread.builder()
                .nickname("작성자").title("제목").content("내용")
                .latitude(37.5).longitude(127.0).gender(Gender.MALE)
                .password("encoded").build();

        given(threadRepository.findById(1L)).willReturn(Optional.of(thread));
        given(passwordGenerator.generate()).willReturn("auto1234");
        given(passwordEncoder.encode("auto1234")).willReturn("encoded");

        Comment comment = Comment.builder().thread(thread).content("댓글내용").password("encoded").build();
        given(commentRepository.save(any())).willReturn(comment);

        var response = commentService.create(1L, new CommentCreateRequest("댓글내용", null));

        assertThat(response.generatedPassword()).isEqualTo("auto1234");
        assertThat(response.content()).isEqualTo("댓글내용");
    }

    @Test
    void create_실패_삭제된스레드() {
        Thread thread = Thread.builder()
                .nickname("작성자").title("제목").content("내용")
                .latitude(37.5).longitude(127.0).gender(Gender.MALE)
                .password("encoded").build();
        thread.softDelete();

        given(threadRepository.findById(1L)).willReturn(Optional.of(thread));

        assertThatThrownBy(() -> commentService.create(1L, new CommentCreateRequest("댓글", null)))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void delete_실패_비밀번호불일치() {
        Thread thread = Thread.builder()
                .nickname("작성자").title("제목").content("내용")
                .latitude(37.5).longitude(127.0).gender(Gender.MALE)
                .password("encoded").build();

        Comment comment = Comment.builder().thread(thread).content("댓글").password("encoded").build();
        given(commentRepository.findById(10L)).willReturn(Optional.of(comment));
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> commentService.delete(1L, 10L, "wrong"))
                .isInstanceOf(InvalidPasswordException.class);
    }
}
