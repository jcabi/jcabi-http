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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link TrustedWire}.
 *
 * @since 1.10
 */
final class TrustedWireTest {

    /**
     * TrustedWire can ignore PKIX errors.
     *
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
     * TrustedWire must leave JVM-wide SSL defaults unchanged during send.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void preservesDefaultSslContextDuringSend() throws Exception {
        final SSLContext before = SSLContext.getDefault();
        final SSLSocketFactory factory = HttpsURLConnection.getDefaultSSLSocketFactory();
        final TrustedWireTest.SslContextCapture capture =
            new TrustedWireTest.SslContextCapture();
        new TrustedWire(capture).send(
            new FakeRequest(),
            "https://localhost/",
            "GET",
            Collections.emptyList(),
            InputStream.nullInputStream(),
            0,
            0
        );
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "TrustedWire must preserve the default context during send",
                capture.captured(),
                Matchers.sameInstance(before)
            ),
            () -> MatcherAssert.assertThat(
                "TrustedWire must preserve the default socket factory during send",
                capture.factory(),
                Matchers.sameInstance(factory)
            )
        );
    }

    /**
     * TrustedWire must restore SSLContext.getDefault() after send completes.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void restoresDefaultSslContextAfterSend() throws Exception {
        final SSLContext before = SSLContext.getDefault();
        new TrustedWire(new TrustedWireTest.SslContextCapture()).send(
            new FakeRequest(),
            "https://localhost/",
            "GET",
            Collections.emptyList(),
            InputStream.nullInputStream(),
            0,
            0
        );
        MatcherAssert.assertThat(
            String.format(
                "TrustedWire must restore the default context %s after send",
                before
            ),
            SSLContext.getDefault(),
            Matchers.sameInstance(before)
        );
    }

    /**
     * Trusted requests must be able to reach their origin concurrently.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void sendsConcurrently() throws Exception {
        final CountDownLatch entered = new CountDownLatch(2);
        final Wire wire = new TrustedWire(
            (req, home, method, headers, content, connect, read) -> {
                entered.countDown();
                try {
                    if (!entered.await(2, TimeUnit.SECONDS)) {
                        throw new IOException("Trusted requests were serialized");
                    }
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException(ex);
                }
                return new FakeRequest().fetch();
            }
        );
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            final List<Future<Response>> responses = Arrays.asList(
                executor.submit(() -> new FakeRequest().through(wire).fetch()),
                executor.submit(() -> new FakeRequest().through(wire).fetch())
            );
            MatcherAssert.assertThat(
                "Both requests must complete concurrently",
                Arrays.asList(
                    responses.get(0).get(5, TimeUnit.SECONDS).status(),
                    responses.get(1).get(5, TimeUnit.SECONDS).status()
                ),
                Matchers.contains(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_OK)
            );
        }
    }

    /**
     * Wire that captures SSLContext.getDefault() at the moment send() runs.
     *
     * @since 1.10
     */
    private static final class SslContextCapture implements Wire {

        /**
         * The SSLContext seen during send.
         */
        private volatile SSLContext context;

        /**
         * The socket factory seen during send.
         */
        private volatile SSLSocketFactory sockets;

        @Override
        public Response send(final Request req, final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final InputStream content,
            final int connect, final int read) throws IOException {
            try {
                this.context = SSLContext.getDefault();
                this.sockets = HttpsURLConnection.getDefaultSSLSocketFactory();
            } catch (final NoSuchAlgorithmException ex) {
                throw new IOException(ex);
            }
            return new FakeRequest().fetch();
        }

        /**
         * The socket factory that was active when send() ran.
         *
         * @return Captured socket factory
         */
        SSLSocketFactory factory() {
            return this.sockets;
        }

        /**
         * The SSLContext that was active when send() ran.
         *
         * @return Captured context
         */
        SSLContext captured() {
            return this.context;
        }
    }
}
