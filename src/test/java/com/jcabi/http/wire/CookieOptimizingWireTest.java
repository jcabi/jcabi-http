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
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CookieOptimizingWire}.
 * @since 1.0
 */
final class CookieOptimizingWireTest {

    /**
     * CookieOptimizingWire can transfer cookies.
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
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, "alpha=boom5")
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            "should be size 1",
            query.headers().get(HttpHeaders.COOKIE),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
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
        );
    }

    /**
     * CookieOptimizingWire can avoid transferring of empty cookies.
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
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, "second=initial-value")
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            "should be size 1",
            query.headers().get(HttpHeaders.COOKIE),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
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
        );
    }

}
