/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

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
     */
    @Test
    void normalizesHeaderKey() {
        MatcherAssert.assertThat(
            "should be 'Content-Type'",
            new ImmutableHeader("content-type", "text/plain").getKey(),
            Matchers.equalTo("Content-Type")
        );
    }

}
