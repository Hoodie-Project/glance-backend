package com.hoodiev.glance.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpExtractorTest {

    @Test
    void XForwardedFor_헤더에서_IP를_추출한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.1");
        request.setRemoteAddr("127.0.0.1");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void XForwardedFor_헤더에_여러_IP가_있으면_첫번째를_반환한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "203.0.113.1, 10.0.0.1, 192.168.1.1");
        request.setRemoteAddr("127.0.0.1");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void IP_앞뒤_공백을_제거한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "  203.0.113.1  , 10.0.0.1");
        request.setRemoteAddr("127.0.0.1");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.1");
    }

    @Test
    void 헤더가_없으면_RemoteAddr를_반환한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.2");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.2");
    }

    @Test
    void 헤더가_공백이면_RemoteAddr를_반환한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Forwarded-For", "   ");
        request.setRemoteAddr("203.0.113.3");

        assertThat(ClientIpExtractor.extract(request)).isEqualTo("203.0.113.3");
    }
}
