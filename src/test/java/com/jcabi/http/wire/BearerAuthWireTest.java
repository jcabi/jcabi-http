/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link BearerAuthWire}.
 *
 * @since 2.0
 */
final class BearerAuthWireTest {
    @Test
    void bearerAuthWireWorks() throws IOException {
        MatcherAssert.assertThat(
            "should be correct header",
            BearerAuthWireTest.authorization("my-bearer-token"),
            Matchers.hasItem("Bearer my-bearer-token")
        );
    }

    @Test
    void onlyOneBearerAuthWireWorks() throws IOException {
        MatcherAssert.assertThat(
            "there should be no more than one 'Authorization' header",
            BearerAuthWireTest.authorization(
                "my-third-bearer-token",
                "my-second-bearer-token",
                "my-first-bearer-token"
            ).size(),
            Matchers.equalTo(1)
        );
    }

    @Test
    void onlyTheFirstBearerAuthWireWorks() throws IOException {
        MatcherAssert.assertThat(
            "should be correct header",
            BearerAuthWireTest.authorization(
                "my-third-bearer-token",
                "my-second-bearer-token",
                "my-first-bearer-token"
            ).get(0),
            Matchers.equalTo("Bearer my-first-bearer-token")
        );
    }

    /**
     * Fetch request and return Authorization headers received by the mock server.
     * @param tokens Bearer tokens to apply as wires
     * @return Authorization headers
     * @throws IOException If request fails
     */
    private static List<String> authorization(final String... tokens)
        throws IOException {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        try {
            Request request = new JdkRequest(
                UriBuilder.fromUri(container.home()).build()
            );
            for (final String token : tokens) {
                request = request.through(BearerAuthWire.class, token);
            }
            request.fetch();
            return container.take().headers().get(HttpHeaders.AUTHORIZATION);
        } finally {
            container.stop();
        }
    }
}
