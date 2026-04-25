package com.hoodiev.glance.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoodiev.glance.comment.dto.CommentCreateRequest;
import com.hoodiev.glance.comment.dto.CommentCreateResponse;
import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.common.exception.InvalidPasswordException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean CommentService commentService;

    @Test
    void POST_comments_성공_201() throws Exception {
        CommentCreateResponse response = new CommentCreateResponse(
                1L, null, "댓글내용", 0, LocalDateTime.now(), "auto1234");

        given(commentService.create(eq(1L), any())).willReturn(response);

        mockMvc.perform(post("/api/threads/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest("닉네임", "댓글내용", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("댓글내용"));
    }

    @Test
    void POST_comments_실패_스레드없음_404() throws Exception {
        given(commentService.create(eq(999L), any())).willThrow(new EntityNotFoundException("Thread", 999L));

        mockMvc.perform(post("/api/threads/999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CommentCreateRequest("닉네임", "댓글", null))))
                .andExpect(status().isNotFound());
    }

    @Test
    void DELETE_comments_실패_비밀번호불일치_403() throws Exception {
        willThrow(new InvalidPasswordException())
                .given(commentService).delete(eq(1L), eq(10L), any());

        mockMvc.perform(delete("/api/threads/1/comments/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrong\"}"))
                .andExpect(status().isForbidden());
    }
}
