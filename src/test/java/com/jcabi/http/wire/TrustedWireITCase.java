/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Integration case for {@link TrustedWire}.
 *
 * @since 1.10.1
 */
final class TrustedWireITCase {

    /**
     * TrustedWire can ignore SSL verifications.
     *
     * @param resource Certificate fixture with SSL problems
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @CsvSource({
        "expired.txt",
        "localhost.txt",
        "untrusted-root.txt"
    })
    void ignoresSslCertProblems(final String resource) throws Exception {
        final HttpsServer server = HttpsServer.create(
            new InetSocketAddress("localhost", 0), 0
        );
        server.setHttpsConfigurator(
            new HttpsConfigurator(TrustedWireHttpsTest.context(resource))
        );
        server.createContext(
            "/", exchange -> {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
                exchange.close();
            }
        );
        server.start();
        try {
            new JdkRequest(
                String.format("https://localhost:%d/", server.getAddress().getPort())
            ).timeout(2000, 2000)
                .through(TrustedWire.class)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } finally {
            server.stop(0);
        }
    }
}
