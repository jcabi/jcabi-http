/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
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
import org.junit.jupiter.api.Assertions;
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
                .withHeader(HttpHeaders.LOCATION, "/"),
            Matchers.any(MkQuery.class),
            Integer.MAX_VALUE
        ).start();
        try {
            final int retries = 3;
            new JdkRequest(container.home())
                .through(AutoRedirectingWire.class, retries)
                .fetch();
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
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void returnsValidResponseAfterRetry() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpStatus.SC_MOVED_TEMPORARILY, "")
                .withHeader(HttpHeaders.LOCATION, "/"),
            Matchers.any(MkQuery.class),
            2
        ).next(new MkAnswer.Simple("success")).start();
        try {
            final RestResponse response = new JdkRequest(container.home())
                .through(AutoRedirectingWire.class)
                .fetch().as(RestResponse.class);
            Assertions.assertAll(
                () -> response.assertBody(Matchers.is("success")),
                () -> response.assertStatus(HttpStatus.SC_OK),
                () -> MatcherAssert.assertThat(
                    "should retries 3 times",
                    container.takeAll(Matchers.any(MkAnswer.class)),
                    Matchers.<MkQuery>iterableWithSize(3)
                )
            );
        } finally {
            container.stop();
        }
    }

    /**
     * AutoRedirectingWire sends cookies set by a redirecting response
     * to the next location.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void sendsCookiesSetWhileRedirecting() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpStatus.SC_MOVED_TEMPORARILY, "")
                .withHeader(HttpHeaders.LOCATION, "/target")
                .withHeader(HttpHeaders.SET_COOKIE, "session=abc; Path=/")
        ).next(new MkAnswer.Simple("success")).start();
        try {
            final RestResponse response = new JdkRequest(container.home())
                .through(AutoRedirectingWire.class)
                .header(HttpHeaders.COOKIE, "session=old")
                .header(HttpHeaders.COOKIE, "theme=dark")
                .fetch().as(RestResponse.class);
            container.take();
            final MkQuery query = container.take();
            Assertions.assertAll(
                () -> response.assertStatus(HttpStatus.SC_OK),
                () -> MatcherAssert.assertThat(
                    "should send the cookie set by the redirect",
                    query.headers().get(HttpHeaders.COOKIE),
                    Matchers.contains("theme=dark; session=abc")
                )
            );
        } finally {
            container.stop();
        }
    }

    /**
     * AutoRedirectingWire doesn't send cookies to a host they don't
     * belong to.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void doesNotSendCookiesToAnotherHost() throws Exception {
        final MkContainer target = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("success")
        ).start();
        final MkContainer origin = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpStatus.SC_MOVED_TEMPORARILY, "").withHeader(
                HttpHeaders.LOCATION,
                String.format("http://127.0.0.1:%d/", target.home().getPort())
            ).withHeader(HttpHeaders.SET_COOKIE, "session=abc")
        ).start();
        try {
            new JdkRequest(origin.home())
                .through(AutoRedirectingWire.class)
                .fetch().as(RestResponse.class)
                .assertStatus(HttpStatus.SC_OK);
            MatcherAssert.assertThat(
                "should not send the cookie to another host",
                target.take().headers(),
                Matchers.not(Matchers.hasKey(HttpHeaders.COOKIE))
            );
        } finally {
            origin.stop();
            target.stop();
        }
    }
}
