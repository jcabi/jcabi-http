/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Template for generic tests for {@link Request}.
 * @since 1.17.4
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
abstract class RequestTestTemplate {

    /**
     * Annotation for a parameterized test case.
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
    static Request request(final URI uri, final Class<? extends Request> type) {
        try {
            return type.getDeclaredConstructor(URI.class).newInstance(uri);
        } catch (final NoSuchMethodException | InstantiationException
            | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException(
                String.format("Failed to make a request of %s", type), ex
            );
        }
    }
}
