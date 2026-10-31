/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration case for {@link BearerAuthWire}.
 *
 * @since 2.0
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
final class BearerAuthWireITCase {

    /**
     * Token.
     */
    private static final String TOKEN = "t0k3nId";

    /**
     * Bearer endpoint.
     */
    private static final String BEARER = "/bearer";

    /**
     * Container with HttpBin.
     */
    private final GenericContainer<?> container = new GenericContainer<>(
        DockerImageName.parse("kennethreitz/httpbin")
    ).withExposedPorts(80);

    @BeforeAll
    void beforeAll() {
        this.container.start();
    }

    @AfterAll
    void tearDown() {
        this.container.stop();
    }

    @Test
    void bearerTokenAuthWorks() throws IOException {
        MatcherAssert.assertThat(
            "token should authenticate",
                this.request(BearerAuthWireITCase.BEARER)
                    .through(BearerAuthWire.class, BearerAuthWireITCase.TOKEN)
                    .fetch()
                    .as(JsonResponse.class)
                    .json()
                    .readObject()
                    .getBoolean("authenticated"),
            Matchers.is(true)
        );
    }

    @Test
    void bearerTokenIsNotSet() throws IOException {
        this.request(BearerAuthWireITCase.BEARER)
            .uri()
            .userInfo("User:Pass")
            .back()
            .through(BasicAuthWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    @Test
    void bearerTokenIsNotSetIfOtherAuthHeaderIsSetFirst() throws IOException {
        this.request(BearerAuthWireITCase.BEARER)
            .uri()
            .userInfo("User:Pass")
            .back()
            .through(BearerAuthWire.class, BearerAuthWireITCase.TOKEN)
            .through(BasicAuthWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
    }

    /**
     * Make request for a specific path.
     * @param path Path.
     * @return Request.
     */
    private Request request(final String path) {
        return new JdkRequest(this.uri().resolve(path));
    }

    /**
     * URI of the container.
     * @return URI.
     */
    private URI uri() {
        return URI.create(
            String.format(
                "http://%s:%d",
                this.container.getHost(),
                this.container.getFirstMappedPort()
            )
        );
    }
}
