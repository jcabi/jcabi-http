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
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test case for {@link CookieOptimizingWire}.
 *
 * @since 1.0
 */
final class CookieOptimizingWireTest {

    /**
     * CookieOptimizingWire ignores cookies without an equals sign.
     *
     * @param cookie Malformed cookie
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @ValueSource(strings = {"", "secure"})
    void ignoresMalformedCookies(final String cookie) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        try {
            new JdkRequest(container.home())
                .through(CookieOptimizingWire.class)
                .header(HttpHeaders.COOKIE, "alpha=one")
                .header(HttpHeaders.COOKIE, cookie)
                .header(HttpHeaders.COOKIE, "beta=two")
                .header(HttpHeaders.COOKIE, "gamma=old")
                .header(HttpHeaders.COOKIE, "gamma=")
                .fetch();
            MatcherAssert.assertThat(
                "should merge valid cookies and remove empty values",
                container.take().headers().get(HttpHeaders.COOKIE),
                Matchers.contains(
                    Matchers.allOf(
                        Matchers.containsString("alpha=one"),
                        Matchers.containsString("beta=two"),
                        Matchers.not(Matchers.containsString("gamma")),
                        Matchers.not(Matchers.containsString("secure"))
                    )
                )
            );
        } finally {
            container.stop();
        }
    }

    /**
     * CookieOptimizingWire can transfer cookies.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void transfersCookiesOnFollow() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withHeader(HttpHeaders.SET_COOKIE, "beta=something; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "alpha=boom1; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "gamma=something; path=/")
                .withHeader(HttpHeaders.LOCATION, "/")
        ).next(new MkAnswer.Simple("")).start();
        final RestResponse response = new JdkRequest(container.home())
            .through(VerboseWire.class)
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, "alpha=boom5")
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch().as(RestResponse.class);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should be size 1",
                query.headers().get(HttpHeaders.COOKIE),
                Matchers.hasSize(1)
            ),
            () -> MatcherAssert.assertThat(
                "should contains 3 items",
                query.headers(),
                Matchers.hasEntry(
                    Matchers.equalTo(HttpHeaders.COOKIE),
                    Matchers.<String>everyItem(
                        Matchers.allOf(
                            Matchers.containsString("beta=something"),
                            Matchers.containsString("gamma=something"),
                            Matchers.containsString("alpha=boom1")
                        )
                    )
                )
            )
        );
    }

    /**
     * CookieOptimizingWire can avoid transferring of empty cookies.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void avoidsTransferringOfEmptyCookiesOnFollow() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withHeader(HttpHeaders.SET_COOKIE, "first=A; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "second=; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "third=B; path=/")
                .withHeader(HttpHeaders.LOCATION, "/a")
        ).next(new MkAnswer.Simple("")).start();
        final RestResponse response = new JdkRequest(container.home())
            .through(VerboseWire.class)
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, "second=initial-value")
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch()
            .as(RestResponse.class);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should be size 1",
                query.headers().get(HttpHeaders.COOKIE),
                Matchers.hasSize(1)
            ),
            () -> MatcherAssert.assertThat(
                "should contains 2 items & not contains 1 item",
                query.headers(),
                Matchers.hasEntry(
                    Matchers.equalTo(HttpHeaders.COOKIE),
                    Matchers.hasItem(
                        Matchers.allOf(
                            Matchers.containsString("first=A"),
                            Matchers.containsString("third=B"),
                            Matchers.not(Matchers.containsString("second"))
                        )
                    )
                )
            )
        );
    }
}
