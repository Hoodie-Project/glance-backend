package com.hoodiev.glance.thread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoodiev.glance.common.exception.EntityNotFoundException;
import com.hoodiev.glance.common.exception.InvalidPasswordException;
import com.hoodiev.glance.common.exception.RateLimitExceededException;
import com.hoodiev.glance.thread.dto.ThreadCreateRequest;
import com.hoodiev.glance.thread.dto.ThreadCreateResponse;
import com.hoodiev.glance.thread.dto.ThreadDetailResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ThreadController.class)
class ThreadControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean ThreadService threadService;

    @Test
    void POST_threads_성공_201() throws Exception {
        ThreadCreateRequest request = new ThreadCreateRequest(
                "테스터", "제목", "내용", 37.5, 127.0, Gender.MALE,
                null, null, null, null);

        ThreadCreateResponse response = new ThreadCreateResponse(
                1L, "테스터", "제목", "내용", 37.5, 127.0,
                null, Gender.MALE, List.of(), java.util.Set.of(), java.util.Set.of(),
                0, 0, LocalDateTime.now(), "auto1234");

        given(threadService.create(any(), anyString())).willReturn(response);

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.generatedPassword").value("auto1234"));
    }

    @Test
    void POST_threads_실패_필수값없음_400() throws Exception {
        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_threads_실패_레이트리밋_429() throws Exception {
        ThreadCreateRequest request = new ThreadCreateRequest(
                "테스터", "제목", "내용", 37.5, 127.0, Gender.MALE,
                null, null, null, null);

        given(threadService.create(any(), anyString())).willThrow(new RateLimitExceededException());

        mockMvc.perform(post("/api/threads")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void GET_threads_목록조회_200() throws Exception {
        given(threadService.getThreads(any(Double.class), any(Double.class), any(), any(), any(), any()))
                .willReturn(Page.empty());

        mockMvc.perform(get("/api/threads")
                        .param("lat", "37.5")
                        .param("lng", "127.0"))
                .andExpect(status().isOk());
    }

    @Test
    void GET_threads_id_상세조회_404() throws Exception {
        given(threadService.getThread(999L)).willThrow(new EntityNotFoundException("Thread", 999L));

        mockMvc.perform(get("/api/threads/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void DELETE_threads_id_비밀번호불일치_403() throws Exception {
        willThrow(new InvalidPasswordException()).given(threadService).delete(any(), any());

        mockMvc.perform(delete("/api/threads/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"wrong\"}"))
                .andExpect(status().isForbidden());
    }
}
