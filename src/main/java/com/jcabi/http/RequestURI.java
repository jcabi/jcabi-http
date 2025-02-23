/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.aspects.Immutable;
import java.net.URI;
import java.util.Map;

/**
 * Request URI.
 *
 * <p>Instance of this interface is returned by {@link Request#uri()},
 * and can be modified using one of the methods below. When modification
 * is done, method {@code back()} returns a modified instance of
 * {@link Request}, for example:
 *
 * <pre> new JdkRequest("http://my.example.com")
 *   .header("Accept", "application/json")
 *   .uri()
 *   .path("/users")
 *   .queryParam("name", "Jeff Lebowski")
 *   .back() // returns a modified instance of Request
 *   .fetch()</pre>
 *
 * <p>Instances of this interface are immutable and thread-safe.
 *
 * @since 0.8
 * @checkstyle AbbreviationAsWordInNameCheck (100 lines)
 */
@Immutable
public interface RequestURI {

    /**
     * Get back to the request it's related to.
     * @return The request we're in
     */
    Request back();

    /**
     * Get URI.
     * @return The destination it is currently pointing to
     */
    URI get();

    /**
     * Set URI.
     * @param uri URI to set
     * @return New alternated URI
     */
    RequestURI set(URI uri);

    /**
     * Add query param.
     * @param name Query param name
     * @param value Value of the query param to set
     * @return New alternated URI
     */
    RequestURI queryParam(String name, Object value);

    /**
     * Add query params.
     * @param map Map of params to add
     * @return New alternated URI
     */
    RequestURI queryParams(Map<String, String> map);

    /**
     * Add URI path.
     * @param segment Path segment to add
     * @return New alternated URI
     */
    RequestURI path(String segment);

    /**
     * Set user info.
     * @param info User info part to set
     * @return New alternated URI
     */
    RequestURI userInfo(String info);

    /**
     * Set port number.
     * @param num The port number to set
     * @return New altered URI
     */
    RequestURI port(int num);

}
