package com.hoodiev.glance.common.util;

import jakarta.servlet.http.HttpServletRequest;

public final class ClientIpExtractor {

    private ClientIpExtractor() {}

    public static String extract(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
