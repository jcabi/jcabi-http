/**
 * Copyright (c) 2011-2014, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jcabi.http;

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.io.InputStream;
import javax.validation.constraints.NotNull;

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
 *   .as(HttpResponse.class)
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 * @see com.jcabi.http.request.JdkRequest
 * @see com.jcabi.http.request.ApacheRequest
 */
@Immutable
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
    @NotNull(message = "URI is never NULL")
    RequestURI uri();

    /**
     * Get request body.
     * @return New alternated request
     */
    @NotNull(message = "request is never NULL")
    RequestBody body();

    /**
     * Set request header.
     * @param name ImmutableHeader name
     * @param value Value of the header to set
     * @return New alternated request
     */
    @NotNull(message = "request is never NULL")
    Request header(
        @NotNull(message = "header name can't be NULL") String name,
        @NotNull(message = "header value can't be NULL") Object value);

    /**
     * Remove all headers with this name.
     * @param name ImmutableHeader name
     * @return New alternated request
     * @since 0.10
     */
    @NotNull(message = "alternated request is never NULL")
    Request reset(@NotNull(message = "header name can't be NULL") String name);

    /**
     * Use this method.
     * @param method The method to use
     * @return New alternated request
     */
    @NotNull(message = "request is never NULL")
    Request method(@NotNull(message = "method can't be NULL") String method);

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

}
