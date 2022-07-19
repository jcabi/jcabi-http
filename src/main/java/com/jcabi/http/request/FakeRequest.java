/*
 * Copyright (c) 2011-2022, jcabi.com
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
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import com.jcabi.http.RequestURI;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.immutable.Array;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Implementation of {@link Request} that always returns the same
 * response, specified in the constructor.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.9
 * // @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "base", "code", "phrase", "hdrs", "content" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class FakeRequest implements Request {

    /**
     * An empty immutable {@code byte} array.
     */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * The Charset to use.
     */
    private static final Charset CHARSET = Charset.forName("UTF-8");

    /**
     * Base request.
     * @checkstyle ParameterNumber (15 lines)
     */
    private final transient Request base;

    /**
     * Status code.
     */
    private final transient int code;

    /**
     * Reason phrase.
     */
    private final transient String phrase;

    /**
     * Headers.
     */
    private final transient Array<Map.Entry<String, String>> hdrs;

    /**
     * Content received.
     */
    @Immutable.Array
    private final transient byte[] content;

    /**
     * Public ctor.
     */
    public FakeRequest() {
        this(
            HttpURLConnection.HTTP_OK,
            "OK",
            Collections.<Map.Entry<String, String>>emptyList(),
            FakeRequest.EMPTY_BYTE_ARRAY
        );
        //@checkstyle ParameterNumber (10 lines)
    }

    /**
     * Public ctor.
     * @param status HTTP status code to return
     * @param reason HTTP reason
     * @param headers HTTP headers
     * @param body HTTP body
     */
    public FakeRequest(final int status, final String reason,
        final Collection<Map.Entry<String, String>> headers,
        final byte[] body) {
        this.code = status;
        this.phrase = reason;
        this.hdrs = new Array<>(headers);
        this.content = body.clone();
        this.base = new BaseRequest(
            new Wire() {
                @Override
                // @checkstyle ParameterNumber (6 lines)
                public Response send(final Request req, final String home,
                    final String method,
                    final Collection<Map.Entry<String, String>> headers,
                    final InputStream text,
                    final int connect,
                    final int read) {
                    return new DefaultResponse(
                        req,
                        FakeRequest.this.code,
                        FakeRequest.this.phrase,
                        FakeRequest.this.hdrs,
                        FakeRequest.this.content
                    );
                }
            },
            "http://localhost:12345/see-FakeRequest-class"
        );
    }

    @Override
    public String toString() {
        return this.base.toString();
    }

    @Override
    public RequestURI uri() {
        return this.base.uri();
    }

    @Override
    public Request header(final String name, final Object value) {
        return this.base.header(name, value);
    }

    @Override
    public Request reset(final String name) {
        return this.base.reset(name);
    }

    @Override
    public RequestBody body() {
        return this.base.body();
    }

    @Override
    public RequestBody multipartBody() {
        return this.base.multipartBody();
    }

    @Override
    public Request method(final String method) {
        return this.base.method(method);
    }

    @Override
    public Request timeout(final int connect, final int read) {
        return this.base.timeout(connect, read);
    }

    @Override
    public Response fetch() throws IOException {
        return this.base.fetch();
    }

    @Override
    public Response fetch(final InputStream stream) throws IOException {
        if (this.content.length > 0) {
            throw new IllegalStateException(
                "Request Body is not empty, use fetch() instead"
            );
        }
        return this.base.fetch(stream);
    }

    @Override
    public <T extends Wire> Request through(final Class<T> type,
        final Object... args) {
        return this.base.through(type, args);
    }

    @Override
    public Request through(final Wire wire) {
        return this.base.through(wire);
    }

    /**
     * Make a similar request, with the provided status code.
     * @param status The code
     * @return New request
     */
    public FakeRequest withStatus(final int status) {
        return new FakeRequest(
            status,
            this.phrase,
            this.hdrs,
            this.content
        );
    }

    /**
     * Make a similar request, with the provided reason line.
     * @param reason Reason line
     * @return New request
     */
    public FakeRequest withReason(final String reason) {
        return new FakeRequest(
            this.code,
            reason,
            this.hdrs,
            this.content
        );
    }

    /**
     * Make a similar request, with the provided HTTP header.
     * @param name Name of the header
     * @param value Value of it
     * @return New request
     */
    public FakeRequest withHeader(final String name, final String value) {
        return new FakeRequest(
            this.code,
            this.phrase,
            this.hdrs.with(new ImmutableHeader(name, value)),
            this.content
        );
    }

    /**
     * Make a similar request, with the provided body.
     * @param text Body
     * @return New request
     */
    public FakeRequest withBody(final String text) {
        return this.withBody(text.getBytes(FakeRequest.CHARSET));
    }

    /**
     * Make a similar request, with the provided body.
     * @param body Body
     * @return New request
     */
    public FakeRequest withBody(final byte[] body) {
        return new FakeRequest(
            this.code,
            this.phrase,
            this.hdrs,
            body
        );
    }

}
