/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.FakeRequest;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
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
     * TrustedWire must replace SSLContext.getDefault() during send so
     * that Apache HTTP client (HttpClients.createSystem()) also trusts
     * all certificates when ApacheRequest is the underlying wire.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void replacesDefaultSslContextDuringSend() throws Exception {
        final SSLContext before = SSLContext.getDefault();
        final SslContextCapture capture = new SslContextCapture();
        new TrustedWire(capture).send(
            new FakeRequest(),
            "https://localhost/",
            "GET",
            Collections.emptyList(),
            InputStream.nullInputStream(),
            0,
            0
        );
        MatcherAssert.assertThat(
            "TrustedWire must replace the default SSL context during send",
            capture.captured(),
            Matchers.not(Matchers.sameInstance(before))
        );
    }

    /**
     * TrustedWire must restore SSLContext.getDefault() after send completes.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void restoresDefaultSslContextAfterSend() throws Exception {
        final SSLContext before = SSLContext.getDefault();
        new TrustedWire(new SslContextCapture()).send(
            new FakeRequest(),
            "https://localhost/",
            "GET",
            Collections.emptyList(),
            InputStream.nullInputStream(),
            0,
            0
        );
        MatcherAssert.assertThat(
            "TrustedWire must restore the default SSL context after send",
            SSLContext.getDefault(),
            Matchers.sameInstance(before)
        );
    }

    /**
     * Wire that captures SSLContext.getDefault() at the moment send() runs.
     * @since 1.10
     */
    private static final class SslContextCapture implements Wire {

        /**
         * The SSLContext captured during send.
         */
        private volatile SSLContext captured;

        @Override
        // @checkstyle ParameterNumber (5 lines)
        public Response send(final Request req, final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final InputStream content,
            final int connect, final int read) throws IOException {
            try {
                this.captured = SSLContext.getDefault();
            } catch (final NoSuchAlgorithmException ex) {
                throw new IOException(ex);
            }
            return new FakeRequest().fetch();
        }

        /**
         * The SSLContext that was active when send() ran.
         * @return Captured context
         */
        SSLContext captured() {
            return this.captured;
        }
    }

}
