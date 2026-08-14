/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import java.net.URI;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * Integration test for {@link Request}.
 * @since 1.17.8
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers(disabledWithoutDocker = true)
final class RequestSecondITCase {

    /**
     * Container with HttpBin.
     */
    @Container
    private static final GenericContainer<?> CONTAINER =
        new GenericContainer<>(
            DockerImageName.parse("kennethreitz/httpbin")
        ).withExposedPorts(80);

    /**
     * URI of the container.
     * @return URI
     */
    private URI uri() {
        return URI.create(
            String.format(
                "http://%s:%d",
                RequestSecondITCase.CONTAINER.getHost(),
                RequestSecondITCase.CONTAINER.getFirstMappedPort()
            )
        );
    }

    /**
     * Test for {@link JdkRequest}.
     * @since 1.17.8
     */
    @Nested
    @SuppressWarnings("PMD.TestClassWithoutTestCases")
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
    @SuppressWarnings("PMD.TestClassWithoutTestCases")
    final class ApacheRequestITCase extends RequestITCaseTemplate {

        ApacheRequestITCase() {
            super(ApacheRequest.class, RequestSecondITCase.this.uri());
        }
    }
}
