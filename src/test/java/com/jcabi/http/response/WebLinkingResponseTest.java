/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.request.FakeRequest;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test case for {@link WebLinkingResponse}.
 * @since 0.9
 */
final class WebLinkingResponseTest {

    /**
     * The Link header.
     */
    private static final String LINK = "Link";

    /**
     * WebLinkingResponse can recognize Links in headers.
     * @param header The value of the Link header
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @ValueSource(
        strings = {
            "</hey/foo>; title=\"Hi!\"; rel=foo",
            "</hey/foo>; title=\"€\"; rel=\"foo\"; media=\"text/xml\""
        }
    )
    void parsesLinksInHeaders(final String header) throws Exception {
        final WebLinkingResponse response = new WebLinkingResponse(
            new FakeRequest()
                .withHeader(WebLinkingResponseTest.LINK, header)
                .fetch()
        );
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should contains '/hey/foo'",
                response.links().get("foo").uri(),
                Matchers.hasToString("/hey/foo")
            ),
            () -> MatcherAssert.assertThat(
                "should contains key 'title'",
                response.links().get("foo"),
                Matchers.hasKey("title")
            ),
            () -> MatcherAssert.assertThat(
                "should not contains key 'something else'",
                response.links(),
                Matchers.not(Matchers.hasKey("something else"))
            )
        );
    }

    /**
     * WebLinkingResponse can follow a link.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void followsLinksInHeaders() throws Exception {
        final WebLinkingResponse response = new WebLinkingResponse(
            new FakeRequest().withHeader(
                WebLinkingResponseTest.LINK,
                "</a>; rel=\"first\", <http://localhost/o>; rel=\"second\""
            ).uri().set(new URI("http://localhost/test")).back().fetch()
        );
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should equals 'http://localhost/a'",
                response.follow("first").uri().get(),
                Matchers.equalTo(new URI("http://localhost/a"))
            ),
            () -> MatcherAssert.assertThat(
                "should equals 'http://localhost/o'",
                response.follow("second").uri().get(),
                Matchers.equalTo(new URI("http://localhost/o"))
            )
        );
    }
}
