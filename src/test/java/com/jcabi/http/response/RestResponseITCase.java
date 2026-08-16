/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Integration test for {@link RestResponse}.
 * @since 1.17.5
 */
final class RestResponseITCase {

    @Test
    void readsCookiesSeveralValues() throws Exception {
        try (
            MkContainer container = new MkGrizzlyContainer().next(
                new MkAnswer.Simple("")
                    .withHeader("Set-Cookie", "ijk=efg")
                    .withHeader("Set-Cookie", "xyz=abc")
            ).start()
        ) {
            final RestResponse resp = new JdkRequest(container.home())
                .fetch().as(RestResponse.class);
            Assertions.assertAll(
                () -> MatcherAssert.assertThat(
                    "should contains value 'efg'",
                    resp.cookie("ijk"),
                    Matchers.hasProperty("value", Matchers.is("efg"))
                ),
                () -> MatcherAssert.assertThat(
                    "should contains value 'abc'",
                    resp.cookie("xyz"),
                    Matchers.hasProperty("value", Matchers.is("abc"))
                )
            );
        }
    }

    @Test
    void readsCookies() throws Exception {
        try (
            MkContainer container = new MkGrizzlyContainer().next(
                new MkAnswer.Simple("").withHeader("Set-Cookie", "foo=bar")
            ).start()
        ) {
            MatcherAssert.assertThat(
                "should contains value 'bar'",
                new JdkRequest(container.home())
                    .fetch()
                    .as(RestResponse.class)
                    .cookie("foo"),
                Matchers.hasProperty("value", Matchers.is("bar"))
            );
        }
    }
}
