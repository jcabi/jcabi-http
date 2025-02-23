/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.google.common.base.Joiner;
import com.jcabi.http.Response;
import com.jcabi.http.request.FakeRequest;
import com.jcabi.matchers.XhtmlMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link JsoupResponse}.
 *
 * @since 1.4
 */
final class JsoupResponseTest {

    /**
     * JsoupResponse normalizes malformed HTML responses.
     * @throws Exception If a problem occurs.
     */
    @Test
    void normalizesHtml() throws Exception {
        final Response resp = new FakeRequest().withBody(
            Joiner.on(' ').join(
                "<html xmlns='http://www.w3.org/1999/xhtml'>",
                "<head><meta name='test'></head>",
                "<p>Hello world"
            )
        ).fetch();
        MatcherAssert.assertThat(
            "should contains normalized response",
            new JsoupResponse(resp).body(),
            XhtmlMatchers.hasXPaths(
                "/xhtml:html/xhtml:head",
                "/xhtml:html/xhtml:body/xhtml:p[.=\"Hello world\"]"
            )
        );
    }

}
