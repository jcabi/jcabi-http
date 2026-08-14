/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import java.nio.charset.StandardCharsets;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link RequestBody}.
 * @since 2.0
 */
final class RequestBodyTest {

    @Test
    void printsEncapsulatedBytes() {
        MatcherAssert.assertThat(
            "cannot print the bytes it encapsulates",
            new RequestBody.Printable(
                "how are you, dude?".getBytes(StandardCharsets.UTF_8)
            ).toString(),
            Matchers.equalTo("how are you, dude?")
        );
    }
}
