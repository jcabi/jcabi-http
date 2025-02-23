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
import java.net.HttpURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link UserAgentWire}.
 * @since 1.2
 */
final class UserAgentWireTest {

    @Test
    void addsDefaultUserAgentHeader() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        new JdkRequest(container.home())
            .through(UserAgentWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            "must add default User-Agent HTTP header",
            container.take().headers(),
            Matchers.hasEntry(
                Matchers.is(HttpHeaders.USER_AGENT),
                Matchers.contains(
                    Matchers.startsWith("jcabi-")
                )
            )
        );
    }

    @Test
    void addsCustomUserAgentHeader() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String agent = "Mozilla/5.0";
        new JdkRequest(container.home())
            .through(UserAgentWire.class, agent)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            "must add custom User-Agent HTTP header",
            container.take().headers(),
            Matchers.hasEntry(
                Matchers.is(HttpHeaders.USER_AGENT),
                Matchers.contains(agent)
            )
        );
    }

}
