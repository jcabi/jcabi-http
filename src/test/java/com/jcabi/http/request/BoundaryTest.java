/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import java.util.Random;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case {@link Boundary}.
 * @since 1.17.3
 */
final class BoundaryTest {

    /**
     * Boundary builds valid string.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    void buildsExpectedBoundary() throws Exception {
        MatcherAssert.assertThat(
            "should be match",
            new Boundary(new Random(0L)).value(),
            Matchers.is("PdAChx6AMjemBZYS_W0fi7l8H_-w-X")
        );
    }
}
