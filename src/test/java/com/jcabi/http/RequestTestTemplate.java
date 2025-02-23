/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;
import lombok.SneakyThrows;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Template for generic tests for {@link Request}.
 *
 * @since 1.17.4
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
abstract class RequestTestTemplate {
    /**
     * Annotation for a parameterized test case.
     *
     * @since 1.17.4
     */
    @Retention(RetentionPolicy.RUNTIME)
    @ValueSource(classes = {ApacheRequest.class, JdkRequest.class})
    protected @interface Values {
    }

    /**
     * Make a request.
     * @param uri URI to start with
     * @param type Type of the request
     * @return Request
     */
    @SneakyThrows
    static Request request(final URI uri, final Class<? extends Request> type) {
        return type.getDeclaredConstructor(URI.class).newInstance(uri);
    }
}
