package com.hoodiev.glance.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpExtractorTest {

    @Test
    void extractsIpFromXForwardedForHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.1");
        request.setRemoteAddr("127.0.0.1");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void extractsFirstIpFromChainedXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1, 192.168.1.1");
        request.setRemoteAddr("127.0.0.1");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void trimsWhitespaceAroundFirstIp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "  203.0.113.1  , 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void fallsBackToRemoteAddrWhenHeaderAbsent() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.2");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.2");
    }

    @Test
    void fallsBackToRemoteAddrWhenHeaderIsBlank() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "   ");
        request.setRemoteAddr("203.0.113.3");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.3");
    }
}
