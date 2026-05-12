/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.FakeRequest;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import javax.net.ssl.SSLContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link TrustedWire}.
 * @since 1.10
 */
final class TrustedWireTest {

    /**
     * TrustedWire can ignore PKIX errors.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void ignoresPkixErrors() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        try {
            new JdkRequest(container.home())
                .through(TrustedWire.class)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } finally {
            container.stop();
        }
    }

    /**
     * TrustedWire must change the default SSL context during send so that
     * Apache HTTP client (used by ApacheRequest) also trusts all certificates.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void setsDefaultSslContextDuringSendForApacheHttpClientCompatibility()
        throws Exception {
        final SSLContext before = SSLContext.getDefault();
        final SSLContext[] during = {null};
        new TrustedWire(
            (req, home, method, headers, content, connect, read) -> {
                try {
                    during[0] = SSLContext.getDefault();
                } catch (final NoSuchAlgorithmException ex) {
                    throw new java.io.IOException(ex);
                }
                return new FakeRequest().fetch();
            }
        ).send(
            new FakeRequest(),
            "https://localhost/",
            "GET",
            Collections.emptyList(),
            new ByteArrayInputStream(new byte[0]),
            0,
            0
        );
        MatcherAssert.assertThat(
            "TrustedWire must replace the default SSL context during send",
            during[0],
            Matchers.not(Matchers.sameInstance(before))
        );
        MatcherAssert.assertThat(
            "TrustedWire must restore the default SSL context after send",
            SSLContext.getDefault(),
            Matchers.sameInstance(before)
        );
    }

}
