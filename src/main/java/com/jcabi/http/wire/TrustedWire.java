/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire that ignores SSL PKIX verifications.
 *
 * <p>This wire ignores :
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(TrustedWire.class)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 1.10
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class TrustedWire implements Wire {

    /**
     * Trust manager.
     */
    private static final TrustManager MANAGER = new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

        @Override
        public void checkClientTrusted(final X509Certificate[] certs,
            final String type) {
            // nothing to check here
        }

        @Override
        public void checkServerTrusted(final X509Certificate[] certs,
            final String types) {
            // nothing to check here
        }
    };

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public TrustedWire(final Wire wire) {
        this.origin = wire;
    }

    // @checkstyle ParameterNumber (13 lines)
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect, final int read) throws IOException {
        synchronized (TrustedWire.class) {
            final SSLSocketFactory def =
                HttpsURLConnection.getDefaultSSLSocketFactory();
            try {
                HttpsURLConnection.setDefaultSSLSocketFactory(
                    TrustedWire.context().getSocketFactory()
                );
                return this.origin.send(
                    req, home, method, headers, content,
                    connect, read
                );
            } finally {
                HttpsURLConnection.setDefaultSSLSocketFactory(def);
            }
        }
    }

    /**
     * Create context.
     * @return Context
     */
    private static SSLContext context() {
        try {
            final SSLContext ctx = SSLContext.getInstance("SSL");
            ctx.init(
                null,
                new TrustManager[]{TrustedWire.MANAGER},
                new SecureRandom()
            );
            return ctx;
        } catch (final KeyManagementException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
