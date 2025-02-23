/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import java.net.URI;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link MkQueryMatchers}.
 *
 * @since 1.5
 */
final class MkQueryMatchersTest {

    /**
     * MkQueryMatchers should be able to match MkQuery body.
     */
    @Test
    void canMatchBody() {
        final String body = "Hello \u20ac!";
        final MkQuery query = Mockito.mock(MkQuery.class);
        Mockito.doReturn(body).when(query).body();
        MatcherAssert.assertThat(
            "should match the query body",
            query,
            MkQueryMatchers.hasBody(
                Matchers.is(body)
            )
        );
    }

    /**
     * MkQueryMatchers should be able to match MkQuery header.
     */
    @Test
    void canMatchHeader() {
        final String header = "Content-Type";
        final String value = "application/json";
        final MkQuery query = Mockito.mock(MkQuery.class);
        Mockito.doReturn(
            Collections.singletonMap(header, Collections.singletonList(value))
        ).when(query).headers();
        MatcherAssert.assertThat(
            "should match the query header",
            query,
            MkQueryMatchers.hasHeader(
                header,
                Matchers.contains(value)
            )
        );
    }

    /**
     * MkQueryMatchers should be able to match MkQuery raw path.
     */
    @Test
    void canMatchPath() {
        final URI body = URI.create("http://example.com/index.html?y=x");
        final MkQuery query = Mockito.mock(MkQuery.class);
        Mockito.doReturn(body).when(query).uri();
        MatcherAssert.assertThat(
            "should match the raw path",
            query,
            MkQueryMatchers.hasPath(
                Matchers.is("/index.html")
            )
        );
    }

    /**
     * MkQueryMatchers should be able to match MkQuery raw query.
     */
    @Test
    void canMatchQuery() {
        final URI body = URI.create("http://example.com/?x=10");
        final MkQuery query = Mockito.mock(MkQuery.class);
        Mockito.doReturn(body).when(query).uri();
        MatcherAssert.assertThat(
            "should match the raw query",
            query,
            MkQueryMatchers.hasQuery(
                Matchers.is("x=10")
            )
        );
    }

}
