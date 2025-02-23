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
import org.junit.jupiter.api.Test;

/**
 * Integration case for {@link BasicAuthWire}.
 *
 * @since 1.17.4
 */
final class BasicAuthWireITCase {
    @Test
    void basicAuthWorks() throws IOException {
        final XmlResponse res = new JdkRequest(
            "https://User:Pass@authenticationtest.com/HTTPAuth/"
        )
            .through(BasicAuthWire.class)
            .through(AutoRedirectingWire.class)
            .fetch()
            .as(XmlResponse.class);
        MatcherAssert.assertThat(
            "should be success",
            res.body(),
            Matchers.containsString("Success!")
        );
    }
}
