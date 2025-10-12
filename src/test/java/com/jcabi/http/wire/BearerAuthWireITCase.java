/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.XmlResponse;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration case for {@link BearerAuthWire}.
 *
 * @since 2.0
 */
final class BearerAuthWireITCase {
    @Test
    void bearerTokenAuthWorks() throws IOException {
        final String token = "t0k3nId";
        final XmlResponse res = new JdkRequest(
            "https://authenticationtest.com"
        )
            .through(BearerAuthWire.class, token)
            .through(AutoRedirectingWire.class)
            .fetch()
            .as(XmlResponse.class);
        MatcherAssert.assertThat(
            "token should be set",
            res.body(),
            Matchers.containsString("Token Set")
        );
    }

    @Disabled
    @Test
    void bearerTokenIsNotSet() throws IOException {
        final XmlResponse res = new JdkRequest(
            "https://User:Pass@authenticationtest.com"
        )
            .through(BasicAuthWire.class)
            .through(AutoRedirectingWire.class)
            .fetch()
            .as(XmlResponse.class);
        MatcherAssert.assertThat(
            "token should not be set",
            res.body(),
            Matchers.containsString("Token Not Set")
        );
    }

    @Disabled
    @Test
    void bearerTokenIsNotSetIfOtherAuthHeaderIsSetFirst() throws IOException {
        final String token = "t0k3nId";
        final XmlResponse res = new JdkRequest(
            "https://User:Pass@authenticationtest.com"
        )
            .through(BearerAuthWire.class, token)
            .through(BasicAuthWire.class)
            .through(AutoRedirectingWire.class)
            .fetch()
            .as(XmlResponse.class);
        MatcherAssert.assertThat(
            "token should not be set",
            res.body(),
            Matchers.containsString("Token Not Set")
        );
    }
}
