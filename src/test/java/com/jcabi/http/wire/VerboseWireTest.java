/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
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
 * Test case for {@link VerboseWire}.
 * @since 1.0
 */
final class VerboseWireTest {

    /**
     * VerboseWire can log requests.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsRequest() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .header(HttpHeaders.USER_AGENT, "it's me")
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
    }

    /**
     * VerboseWire can log request body.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsRequestBody() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        try {
            new JdkRequest(container.home())
                .through(VerboseWire.class)
                .method(Request.POST)
                .body().set("hello, world!").back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
            final MkQuery query = container.take();
            MatcherAssert.assertThat(
                "should starts with 'hello,'",
                query.body(),
                Matchers.startsWith("hello,")
            );
        } finally {
            container.stop();
        }
    }

}
