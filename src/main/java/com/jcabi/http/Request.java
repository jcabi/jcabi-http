/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.io.InputStream;

/**
 * RESTful request.
 *
 * <p>Instance of this class is supposed to be used this way:
 *
 * <pre> String name = new ApacheRequest("https://www.example.com:8080")
 *   .uri().path("/users").queryParam("id", 333).back()
 *   .method(Request.GET)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
 *   .fetch()
 *   .as(RestResponse.class)
 *   .assertStatus(HttpURLConnection.HTTP_OK)
 *   .as(XmlResponse.class)
 *   .assertXPath("/page/links/link[@rel='see']")
 *   .rel("/page/links/link[@rel='see']/@href")
 *   .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
 *   .fetch()
 *   .as(JsonResponse.class)
 *   .json().getJsonObject().getString("name");</pre>
 *
 * <p>Since version 0.10 it is recommended to use
 * {@link com.jcabi.http.wire.RetryWire}
 * decorator to avoid accidental {@link IOException} when connection is weak
 * or unstable, for example:
 *
 * <pre> String body = new JdkRequest("https://www.google.com")
 *   .through(RetryWire.class)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>Instances of this interface are immutable and thread-safe.
 *
 * <p>You can use either ApacheRequest or JdkRequest,
 * according to your needs. JdkRequest won't require any additional
 * dependencies, while ApacheRequest will properly support all
 * possible HTTP methods (JdkRequest doesn't support {@code PATCH},
 * for example).
 *
 * @see com.jcabi.http.request.JdkRequest
 * @see com.jcabi.http.request.ApacheRequest
 * @since 0.8
 */
@Immutable
@SuppressWarnings({"PMD.AvoidDuplicateLiterals", "PMD.TooManyMethods"})
public interface Request {

    /**
     * GET method name.
     */
    String GET = "GET";

    /**
     * POST method name.
     */
    String POST = "POST";

    /**
     * PUT method name.
     */
    String PUT = "PUT";

    /**
     * HEAD method name.
     */
    String HEAD = "HEAD";

    /**
     * DELETE method name.
     */
    String DELETE = "DELETE";

    /**
     * OPTIONS method name.
     */
    String OPTIONS = "OPTIONS";

    /**
     * PATCH method name.
     */
    String PATCH = "PATCH";

    /**
     * Get destination URI.
     * @return The destination it is currently pointing to
     */
    RequestURI uri();

    /**
     * Get request body.
     * @return New alternated request
     */
    RequestBody body();

    /**
     * Get multipart form (multipart/form-data) body.
     * @return New altered request
     */
    RequestBody multipartBody();

    /**
     * Set request header.
     * @param name ImmutableHeader name
     * @param value Value of the header to set
     * @return New alternated request
     */
    Request header(String name, Object value);

    /**
     * Remove all headers with this name.
     * @param name ImmutableHeader name
     * @return New alternated request
     * @since 0.10
     */
    Request reset(String name);

    /**
     * Use this method.
     * @param method The method to use
     * @return New alternated request
     */
    Request method(String method);

    /**
     * Use this timeout values.
     * @param connect The connect timeout to use in ms
     * @param read The read timeout to use in ms
     * @return New alternated request
     */
    Request timeout(int connect, int read);

    /**
     * Execute it with a specified HTTP method.
     * @return Response
     * @throws IOException If fails to fetch HTTP request
     */
    Response fetch() throws IOException;

    /**
     * Execute this request using the content provided by the
     * {@link InputStream} being passed as the request body. Note that the
     * request MUST have an empty body when this method is being invoked, or
     * it will throw an {@link IllegalStateException}.
     *
     * @param stream The input stream to use
     * @return Response
     * @throws IOException If fails to fetch HTTP request
     * @since 1.8
     */
    Response fetch(InputStream stream) throws IOException;

    /**
     * Send it through a decorating {@link Wire}.
     * @param type Type of wire to use
     * @param args Optional arguments for the wire constructor
     * @param <T> Type to use
     * @return New request with a wire decorated
     * @since 0.10
     */
    <T extends Wire> Request through(Class<T> type, Object... args);

    /**
     * Send it through a decorating {@link Wire}.
     * @param wire Wire to use
     * @return New request with a wire decorated
     * @since 0.10
     */
    Request through(Wire wire);
}
