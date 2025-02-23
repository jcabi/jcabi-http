/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import jakarta.ws.rs.core.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link AutoRedirectingWire}.
 *
 * @since 1.7
 */
final class AutoRedirectingWireTest {

    /**
     * AutoRedirectingWire retries up to the specified number of times for
     * HTTP Status 3xx responses.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void retriesForHttpRedirectStatus() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpStatus.SC_MOVED_TEMPORARILY, "")
                // @checkstyle MultipleStringLiteralsCheck (1 line)
                .withHeader(HttpHeaders.LOCATION, "/"),
            Matchers.any(MkQuery.class),
            Integer.MAX_VALUE
        ).start();
        try {
            final int retries = 3;
            new JdkRequest(container.home())
                .through(AutoRedirectingWire.class, retries)
                .fetch().as(RestResponse.class)
                .assertStatus(HttpStatus.SC_MOVED_TEMPORARILY);
            MatcherAssert.assertThat(
                "should retries 3 times",
                container.takeAll(Matchers.any(MkAnswer.class)),
                Matchers.<MkQuery>iterableWithSize(retries)
            );
        } finally {
            container.stop();
        }
    }

    /**
     * AutoRedirectingWire will retry a few times and immediately return if
     * a valid response is obtained.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void returnsValidResponseAfterRetry() throws Exception {
        final String body = "success";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpStatus.SC_MOVED_TEMPORARILY, "")
                .withHeader(HttpHeaders.LOCATION, "/"),
            Matchers.any(MkQuery.class),
            2
        ).next(new MkAnswer.Simple(body)).start();
        try {
            new JdkRequest(container.home())
                .through(AutoRedirectingWire.class)
                .fetch().as(RestResponse.class)
                .assertBody(Matchers.is(body))
                .assertStatus(HttpStatus.SC_OK);
            MatcherAssert.assertThat(
                "should retries 3 times",
                container.takeAll(Matchers.any(MkAnswer.class)),
                Matchers.<MkQuery>iterableWithSize(3)
            );
        } finally {
            container.stop();
        }
    }

}
