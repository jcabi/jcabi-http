/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import java.net.URI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test for {@link Request}.
 *
 * @since 1.17.8
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
final class RequestSecondITCase {

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

    /**
     * Test for {@link JdkRequest}.
     * @since 1.17.8
     */
    @Nested
    final class JdkRequestITCase extends RequestITCaseTemplate {
        JdkRequestITCase() {
            super(JdkRequest.class, RequestSecondITCase.this.uri());
        }
    }

    /**
     * Test for {@link ApacheRequest}.
     * @since 1.17.8
     */
    @Nested
    final class ApacheRequestITCase extends RequestITCaseTemplate {
        ApacheRequestITCase() {
            super(ApacheRequest.class, RequestSecondITCase.this.uri());
        }
    }
}
