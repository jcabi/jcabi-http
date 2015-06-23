/**
 * Copyright (c) 2011-2015, jcabi.com
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
import com.jcabi.http.Constants;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Implementation of {@link Request} that always returns the same
 * response, specified in the constructor.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.9
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = { "base", "code", "phrase", "hdrs", "content" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class FakeRequest implements Request {

    /**
     * Base request.
     * @checkstyle ParameterNumber (15 lines)
     */
    private final transient Request base = new BaseRequest(
        new Wire() {
            @Override
            public Response send(final Request req, final String home,
                final String method,
                final Collection<Map.Entry<String, String>> headers,
                final InputStream text) throws IOException {
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
            Constants.EMPTY_BYTE_ARRAY
        );
    }

    /**
     * Public ctor.
     * @param status HTTP status code to return
     * @param reason HTTP reason
     * @param headers HTTP headers
     * @param body HTTP body
     * @checkstyle ParameterNumber (10 lines)
     */
    public FakeRequest(final int status,
        @NotNull(message = "HTTP reason can't be NULL") final String reason,
        @NotNull(message = "list of headers can't be NULL")
        final Collection<Map.Entry<String, String>> headers,
        @NotNull(message = "body can't be NULL") final byte[] body) {
        this.code = status;
        this.phrase = reason;
        this.hdrs = new Array<Map.Entry<String, String>>(headers);
        this.content = body.clone();
    }

    @Override
    public String toString() {
        return this.base.toString();
    }

    @Override
    @NotNull
    public RequestURI uri() {
        return this.base.uri();
    }

    @Override
    public Request header(
        @NotNull(message = "header name can't be NULL") final String name,
        @NotNull(message = "header value can't be NULL") final Object value) {
        return this.base.header(name, value);
    }

    @Override
    public Request reset(
        @NotNull(message = "header name can't be NULL") final String name) {
        return this.base.reset(name);
    }

    @Override
    public RequestBody body() {
        return this.base.body();
    }

    @Override
    public Request method(
        @NotNull(message = "method can't be NULL") final String method) {
        return this.base.method(method);
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
        return this.withBody(text.getBytes(Constants.CHARSET));
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
