package com.hoodiev.glance.dto.common;

import jakarta.validation.constraints.NotBlank;

public record DeleteRequest(
        @NotBlank String password
) {}
