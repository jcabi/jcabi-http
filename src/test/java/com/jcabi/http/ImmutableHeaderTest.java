/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import java.util.Map;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ImmutableHeader}.
 * @since 1.1
 */
final class ImmutableHeaderTest {

    /**
     * ImmutableHeader can normalize headers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void normalizesHeaderKey() throws Exception {
        final Map.Entry<String, String> header =
            new ImmutableHeader("content-type", "text/plain");
        MatcherAssert.assertThat(
            "should be 'Content-Type'",
            header.getKey(),
            Matchers.equalTo("Content-Type")
        );
    }

}
