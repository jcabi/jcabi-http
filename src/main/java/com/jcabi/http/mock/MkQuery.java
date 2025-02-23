/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import com.jcabi.aspects.Immutable;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Mock HTTP query/request.
 *
 * @since 0.10
 */
@Immutable
public interface MkQuery {

    /**
     * URI.
     * @return URI
     */
    URI uri();

    /**
     * HTTP method.
     * @return Method
     */
    String method();

    /**
     * Headers.
     * @return Headers
     */
    Map<String, List<String>> headers();

    /**
     * HTTP request body as String.
     * @return Body
     */
    String body();

    /**
     * HTTP request body as byte array.
     * @return Body
     * @since 1.13
     */
    byte[] binary();
}
