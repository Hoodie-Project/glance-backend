package com.hoodiev.glance.common.util;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void generatesEightCharacterPassword() {
        assertThat(generator.generate()).hasSize(8);
    }

    @Test
    void doesNotContainAmbiguousCharacters() {
        Set<String> passwords = IntStream.range(0, 200)
                .mapToObj(i -> generator.generate())
                .collect(Collectors.toSet());

        for (String pw : passwords) {
            assertThat(pw).doesNotContain("0", "1", "I", "O", "l", "o");
        }
    }

    @Test
    void producesUniquePasswords() {
        Set<String> passwords = IntStream.range(0, 50)
                .mapToObj(i -> generator.generate())
                .collect(Collectors.toSet());

        assertThat(passwords).hasSizeGreaterThan(1);
    }
}
