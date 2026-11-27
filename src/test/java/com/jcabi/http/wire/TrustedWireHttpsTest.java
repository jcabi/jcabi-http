/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import jakarta.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Request-local SSL integration tests without external services.
 *
 * @since 2.0
 */
final class TrustedWireHttpsTest {

    /**
     * Both transports trust a self-signed server only through TrustedWire.
     *
     * @param factory Request factory
     * @throws Exception If something goes wrong
     */
    @ParameterizedTest
    @MethodSource("requests")
    void trustsOnlyDecoratedRequests(final Function<URI, Request> factory) throws Exception {
        final HttpsServer server = HttpsServer.create(
            new InetSocketAddress("localhost", 0), 0
        );
        server.setHttpsConfigurator(
            new HttpsConfigurator(TrustedWireHttpsTest.context("localhost.txt"))
        );
        server.createContext(
            "/", exchange -> {
                exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
                exchange.close();
            }
        );
        server.start();
        try {
            final Request request = factory.apply(
                URI.create(String.format("https://localhost:%d/", server.getAddress().getPort()))
            ).timeout(2000, 2000);
            Assertions.assertThrows(
                IOException.class, request::fetch,
                "Ordinary requests must reject the self-signed certificate"
            );
            MatcherAssert.assertThat(
                "Trusted requests must accept the self-signed certificate",
                request.through(BasicAuthWire.class).through(TrustedWire.class)
                    .fetch().status(),
                Matchers.equalTo(HttpURLConnection.HTTP_OK)
            );
            Assertions.assertThrows(
                IOException.class, request::fetch,
                "Trusting one request must not affect later ordinary requests"
            );
        } finally {
            server.stop(0);
        }
    }

    /**
     * Load a server context from a hex-encoded PKCS12 fixture.
     *
     * @param resource Fixture name, with password "password"
     * @return Server context
     * @throws Exception If the fixture cannot be loaded
     */
    static SSLContext context(final String resource) throws Exception {
        final char[] password = "password".toCharArray();
        final KeyStore keys = KeyStore.getInstance("PKCS12");
        try (InputStream input = TrustedWireHttpsTest.class.getResourceAsStream(resource)) {
            keys.load(
                new ByteArrayInputStream(
                    DatatypeConverter.parseHexBinary(
                        new String(input.readAllBytes(), StandardCharsets.US_ASCII).trim()
                    )
                ), password
            );
        }
        final KeyManagerFactory managers = KeyManagerFactory.getInstance(
            KeyManagerFactory.getDefaultAlgorithm()
        );
        managers.init(keys, password);
        final SSLContext context = SSLContext.getInstance("TLS");
        context.init(managers.getKeyManagers(), null, null);
        return context;
    }

    private static Stream<Function<URI, Request>> requests() {
        return Stream.of(JdkRequest::new, ApacheRequest::new);
    }
}
