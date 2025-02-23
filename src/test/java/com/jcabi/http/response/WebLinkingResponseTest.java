/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.request.FakeRequest;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

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
     * @throws Exception If something goes wrong inside
     */
    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void parsesLinksInHeaders() throws Exception {
        final String[] headers = {
            "</hey/foo>; title=\"Hi!\"; rel=foo",
            "</hey/foo>; title=\"\u20ac\"; rel=\"foo\"; media=\"text/xml\"",
        };
        for (final String header : headers) {
            final WebLinkingResponse response = new WebLinkingResponse(
                new FakeRequest()
                    .withHeader(WebLinkingResponseTest.LINK, header)
                    .fetch()
            );
            final WebLinkingResponse.Link link = response.links().get("foo");
            MatcherAssert.assertThat(
                "should contains '/hey/foo'",
                link.uri(),
                Matchers.hasToString("/hey/foo")
            );
            MatcherAssert.assertThat(
                "should contains key 'title'",
                link,
                Matchers.hasKey("title")
            );
            MatcherAssert.assertThat(
                "should not contains key 'something else'",
                response.links(),
                Matchers.not(Matchers.hasKey("something else"))
            );
        }
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
        MatcherAssert.assertThat(
            "should equals 'http://localhost/a'",
            response.follow("first").uri().get(),
            Matchers.equalTo(new URI("http://localhost/a"))
        );
        MatcherAssert.assertThat(
            "should equals 'http://localhost/o'",
            response.follow("second").uri().get(),
            Matchers.equalTo(new URI("http://localhost/o"))
        );
    }

}
