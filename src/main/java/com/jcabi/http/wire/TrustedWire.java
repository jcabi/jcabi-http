/*
 * Copyright (c) 2011-2022, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
