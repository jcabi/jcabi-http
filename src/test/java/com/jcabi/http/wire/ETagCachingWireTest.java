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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
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
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(body)
            .withHeader(HttpHeaders.ETAG, "3e25")
        ).next(
            new MkAnswer.Simple("")
            .withStatus(HttpURLConnection.HTTP_NOT_MODIFIED)
        ).start();
        final Request req =
            new JdkRequest(container.home()).through(ETagCachingWire.class);
        final Collection<String> bodies = new ArrayList<>(2);
        bodies.add(req.fetch().body());
        bodies.add(req.fetch().body());
        container.stop();
        MatcherAssert.assertThat(
            "should serve the same content twice",
            bodies,
            Matchers.contains(body, body)
        );
    }

    /**
     * ETagCachingWire can detect content modification.
     * @throws IOException If something goes wrong inside
     */
    @Test
    void detectsContentModification() throws IOException {
        final String before = "before change";
        final String after = "after change";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(before)
                .withHeader(HttpHeaders.ETAG, "3e26")
        ).next(
            new MkAnswer.Simple(after)
                .withHeader(HttpHeaders.ETAG, "3e27")
        ).start();
        final Request req =
            new JdkRequest(container.home())
                .through(ETagCachingWire.class);
        final Collection<String> bodies = new ArrayList<>(2);
        bodies.add(req.fetch().body());
        bodies.add(req.fetch().body());
        container.stop();
        MatcherAssert.assertThat(
            "should detect the modification",
            bodies,
            Matchers.contains(before, after)
        );
    }
}
