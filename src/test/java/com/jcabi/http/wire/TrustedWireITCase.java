/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Integration case for {@link TrustedWire}.
 *
 * @since 1.10.1
 */
final class TrustedWireITCase {

    /**
     * TrustedWire can ignore SSL verifications against local certificates.
     * The PKCS12 fixtures use the test-only password "changeit" and contain
     * expired, self-signed, and untrusted-root certificates for localhost.
     *
     * @param fixture Keystore with SSL problems
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @ValueSource(strings = {"expired", "self-signed", "untrusted-root"})
    void ignoresSslCertProblems(final String fixture) throws Exception {
        final char[] password = "changeit".toCharArray();
        final KeyStore keys = KeyStore.getInstance("PKCS12");
        try (
            InputStream input = TrustedWireITCase.class.getResourceAsStream(
                String.format("/ssl/%s.p12", fixture)
            )
        ) {
            keys.load(input, password);
        }
        final KeyManagerFactory factory = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        );
        factory.init(keys, password);
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(factory.getKeyManagers(), null, null);
        final HttpsServer server = HttpsServer.create(
            new InetSocketAddress("localhost", 0), 0
        );
        server.setHttpsConfigurator(new HttpsConfigurator(context));
        server.createContext(
            "/", exchange -> {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
                exchange.close();
            }
        );
        server.start();
        try {
            final String url = String.format(
                "https://localhost:%d/", server.getAddress().getPort()
            );
            Assertions.assertThrows(
                IOException.class, () -> new JdkRequest(url).fetch()
            );
            new JdkRequest(url)
                .through(TrustedWire.class)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } finally {
            server.stop(0);
        }
    }
}
