/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.request.JdkRequest;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Integration test for {@link RestResponse}.
 *
 * @since 1.17.5
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals") final class RestResponseITCase {
    @Test
    void readsCookiesSeveralValues() throws IOException {
        final RestResponse resp = new JdkRequest(
            "https://httpbin.org/cookies/set?ijk=efg&xyz=abc"
        )
            .fetch()
            .as(RestResponse.class);
        Assertions.assertAll(
            new Executable() {
                @Override
                public void execute() {
                    MatcherAssert.assertThat(
                        "should contains value 'efg'",
                        resp.cookie("ijk"),
                        Matchers.hasProperty("value", Matchers.is("efg"))
                    );
                }
            },
            new Executable() {
                @Override
                public void execute() {
                    MatcherAssert.assertThat(
                        "should contains value 'abc'",
                        resp.cookie("xyz"),
                        Matchers.hasProperty("value", Matchers.is("abc"))
                    );
                }
            }
        );
    }

    @Test
    void readsCookies() throws IOException {
        MatcherAssert.assertThat(
            "should contains value 'bar'",
            new JdkRequest("https://httpbin.org/cookies/set?foo=bar")
                .fetch()
                .as(RestResponse.class)
                .cookie("foo"),
            Matchers.hasProperty("value", Matchers.is("bar"))
        );
    }
}
