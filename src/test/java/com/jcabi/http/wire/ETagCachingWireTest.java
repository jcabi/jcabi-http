/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link ETagCachingWire}.
 * @since 2.0
 */
final class ETagCachingWireTest {

    /**
     * ETagCachingWire can take content from cache.
     * @throws IOException If something goes wrong inside
     */
    @Test
    void takesContentFromCache() throws IOException {
        final String body = "sample content";
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(body)
                .withHeader(HttpHeaders.ETAG, "3e25")
            )
            .next(
                new MkAnswer.Simple("")
                .withStatus(HttpURLConnection.HTTP_NOT_MODIFIED)
            )
            .start();
        final Request req =
            new JdkRequest(container.home()).through(ETagCachingWire.class);
        req
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.equalTo(body));
        req
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.equalTo(body));
        container.stop();
    }

    /**
     * ETagCachingWire can detect content modification.
     * @throws IOException If something goes wrong inside
     */
    @Test
    void detectsContentModification() throws IOException {
        final String before = "before change";
        final String after = "after change";
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(before)
                    .withHeader(HttpHeaders.ETAG, "3e26")
            )
            .next(
                new MkAnswer.Simple(after)
                    .withHeader(HttpHeaders.ETAG, "3e27")
            )
            .start();
        final Request req =
            new JdkRequest(container.home())
                .through(ETagCachingWire.class);
        req
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.equalTo(before));
        req
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.equalTo(after));
        container.stop();
    }
}
