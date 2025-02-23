/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link MkAnswerMatchers}.
 * @since 1.5
 */
final class MkAnswerMatchersTest {

    /**
     * MkAnswerMatchers should be able to match MkAnswer body.
     */
    @Test
    void canMatchBody() {
        final String body = "Hello \u20ac!";
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(body).when(query).body();
        MatcherAssert.assertThat(
            "should match the answer body",
            query,
            MkAnswerMatchers.hasBody(
                Matchers.is(body)
            )
        );
    }

    /**
     * MkAnswerMatchers can match MkAnswer body bytes.
     */
    @Test
    void canMatchBodyBytes() {
        final byte[] body = {0x01, 0x45, 0x21};
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(body).when(query).bodyBytes();
        MatcherAssert.assertThat(
            "should match the answer body bytes",
            query,
            MkAnswerMatchers.hasBodyBytes(
                Matchers.is(body)
            )
        );
    }

    /**
     * MkAnswerMatchers should be able to match MkAnswer header.
     */
    @Test
    void canMatchHeader() {
        final String header = "Content-Type";
        final String value = "application/json";
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(
            Collections.singletonMap(header, Collections.singletonList(value))
        ).when(query).headers();
        MatcherAssert.assertThat(
            "should match the answer header",
            query,
            MkAnswerMatchers.hasHeader(
                header,
                Matchers.contains(value)
            )
        );
    }
}
