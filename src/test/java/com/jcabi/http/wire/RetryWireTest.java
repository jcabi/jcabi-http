/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import jakarta.ws.rs.core.UriBuilder;
import java.net.HttpURLConnection;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Test case for {@link RetryWire}.
 *
 * @since 1.2
 */
final class RetryWireTest {

    /**
     * RetryWire can make a few requests before giving up.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void makesMultipleRequests() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
            .start();
        new JdkRequest(container.home())
            .through(RetryWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
    }

    /**
     * RetryWire should strip user info when logging URL.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void stripsUserInfoWhenLogging() throws Exception {
        final Logger logger = (Logger) LoggerFactory.getLogger(RetryWire.class);
        final ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
        try (MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
            .start()) {
            final URI home = container.home();
            new JdkRequest(UriBuilder.fromUri(home).userInfo("jeff:ffej").toString())
                .through(RetryWire.class)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
            MatcherAssert.assertThat(
                "should strips user info",
                appender.list,
                Matchers.hasItem(
                    Matchers.hasProperty(
                        "message",
                        Matchers.containsString(
                            String.format("GET %s (auth: j***j)", home)
                        )
                    )
                )
            );
        }
    }

}
