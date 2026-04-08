package com.hoodiev.glance.dto.thread;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ThreadCreateRequest(
        @NotBlank @Size(max = 30) String title,
        @NotBlank @Size(max = 500) String content,
        @NotNull Double latitude,
        @NotNull Double longitude,
        @NotBlank @Size(min = 4) String password,
        @Size(max = 5) List<@NotBlank String> tags
) {}
