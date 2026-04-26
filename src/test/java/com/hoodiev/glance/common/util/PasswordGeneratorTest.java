package com.hoodiev.glance.common.util;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void 비밀번호는_8자리다() {
        assertThat(generator.generate()).hasSize(8);
    }

    @Test
    void 헷갈리는_문자가_포함되지_않는다() {
        Set<String> passwords = IntStream.range(0, 200)
                .mapToObj(i -> generator.generate())
                .collect(Collectors.toSet());

        for (String pw : passwords) {
            assertThat(pw).doesNotContain("0", "1", "I", "O", "l", "o");
        }
    }

    @Test
    void 매번_다른_비밀번호가_생성된다() {
        Set<String> passwords = IntStream.range(0, 50)
                .mapToObj(i -> generator.generate())
                .collect(Collectors.toSet());

        assertThat(passwords).hasSizeGreaterThan(1);
    }
}
