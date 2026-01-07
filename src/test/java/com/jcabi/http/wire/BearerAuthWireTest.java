/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.HttpURLConnection;
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
        final String token = "my-bearer-token";
        final String expected = "Bearer my-bearer-token";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        new JdkRequest(UriBuilder.fromUri(container.home()).build())
            .through(BearerAuthWire.class, token)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            "should be correct header",
            container.take().headers().get(HttpHeaders.AUTHORIZATION).get(0),
            Matchers.equalTo(expected)
        );
    }

    @Test
    void onlyOneBearerAuthWireWorks() throws IOException {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        new JdkRequest(UriBuilder.fromUri(container.home()).build())
            .through(BearerAuthWire.class, "my-third-bearer-token")
            .through(BearerAuthWire.class, "my-second-bearer-token")
            .through(BearerAuthWire.class, "my-first-bearer-token")
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            "there should be no more than one 'Authorization' header",
            container.take().headers().get(HttpHeaders.AUTHORIZATION).size(),
            Matchers.equalTo(1)
        );
    }

    @Test
    void onlyTheFirstBearerAuthWireWorks() throws IOException {
        final String expected = "Bearer my-first-bearer-token";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        new JdkRequest(UriBuilder.fromUri(container.home()).build())
            .through(BearerAuthWire.class, "my-third-bearer-token")
            .through(BearerAuthWire.class, "my-second-bearer-token")
            .through(BearerAuthWire.class, "my-first-bearer-token")
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            "should be correct header",
            container.take().headers().get(HttpHeaders.AUTHORIZATION).get(0),
            Matchers.equalTo(expected)
        );
    }
}
