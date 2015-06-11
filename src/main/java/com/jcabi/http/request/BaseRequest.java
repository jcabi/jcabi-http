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
import com.jcabi.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonStructure;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.UriBuilder;
import lombok.EqualsAndHashCode;

/**
 * Base implementation of {@link Request}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @see Request
 * @see Response
 */
@Immutable
@EqualsAndHashCode(of = { "home", "mtd", "hdrs", "content" })
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
final class BaseRequest implements Request {

    /**
     * Wire to use.
     */
    private final transient Wire wire;

    /**
     * Request URI.
     */
    private final transient String home;

    /**
     * Method to use.
     */
    private final transient String mtd;

    /**
     * Headers.
     */
    private final transient Array<Map.Entry<String, String>> hdrs;

    /**
     * Body to use.
     */
    @Immutable.Array
    private final transient byte[] content;

    /**
     * Public ctor.
     * @param wre Wire
     * @param uri The resource to work with
     */
    BaseRequest(final Wire wre, final String uri) {
        this(
            wre, uri,
            new Array<Map.Entry<String, String>>(),
            Constants.GET, Constants.EMPTY_BYTE_ARRAY
        );
    }

    /**
     * Public ctor.
     * @param wre Wire
     * @param uri The resource to work with
     * @param headers Headers
     * @param method HTTP method
     * @param body HTTP request body
     * @checkstyle ParameterNumber (5 lines)
     */
    BaseRequest(final Wire wre, final String uri,
        final Iterable<Map.Entry<String, String>> headers,
        final String method, final byte[] body) {
        this.wire = wre;
        URI addr = URI.create(uri);
        if (addr.getPath().isEmpty()) {
            addr = UriBuilder.fromUri(addr).path("/").build();
        }
        this.home = addr.toString();
        this.hdrs = new Array<Map.Entry<String, String>>(headers);
        this.mtd = method;
        this.content = body.clone();
    }

    @Override
    @NotNull
    public RequestURI uri() {
        return new BaseRequest.BaseURI(this, this.home);
    }

    @Override
    public Request header(
        @NotNull(message = "header name can't be NULL") final String name,
        @NotNull(message = "header value can't be NULL") final Object value) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs.with(new ImmutableHeader(name, value.toString())),
            this.mtd,
            this.content
        );
    }

    @Override
    public Request reset(
        @NotNull(message = "header name can't be NULL") final String name) {
        final Collection<Map.Entry<String, String>> headers =
            new LinkedList<Map.Entry<String, String>>();
        final String key = ImmutableHeader.normalize(name);
        for (final Map.Entry<String, String> header : this.hdrs) {
            if (!header.getKey().equals(key)) {
                headers.add(header);
            }
        }
        return new BaseRequest(
            this.wire,
            this.home,
            headers,
            this.mtd,
            this.content
        );
    }

    @Override
    public RequestBody body() {
        return new BaseRequest.BaseBody(this, this.content);
    }

    @Override
    public Request method(
        @NotNull(message = "method can't be NULL") final String method) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs,
            method,
            this.content
        );
    }

    @Override
    public Response fetch() throws IOException {
        return this.fetchResponse(new ByteArrayInputStream(this.content));
    }

    @Override
    public Response fetch(final InputStream stream) throws IOException {
        if (this.content.length > 0) {
            throw new IllegalStateException(
                "Request Body is not empty, use fetch() instead"
            );
        }
        return this.fetchResponse(stream);
    }

    @Override
    public <T extends Wire> Request through(final Class<T> type,
        final Object... args) {
        Constructor<?> ctor = null;
        for (final Constructor<?> opt : type.getDeclaredConstructors()) {
            if (opt.getParameterTypes().length == args.length + 1) {
                ctor = opt;
                break;
            }
        }
        if (ctor == null) {
            throw new IllegalArgumentException(
                String.format(
                    "class %s doesn't have a ctor with %d argument(s)",
                    type.getName(), args.length
                )
            );
        }
        final Object[] params = new Object[args.length + 1];
        params[0] = this.wire;
        System.arraycopy(args, 0, params, 1, args.length);
        final Wire decorated;
        try {
            decorated = Wire.class.cast(ctor.newInstance(params));
        } catch (final InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
        return new BaseRequest(
            decorated,
            this.home,
            this.hdrs,
            this.mtd,
            this.content
        );
    }

    @Override
    public String toString() {
        final URI uri = URI.create(this.home);
        final StringBuilder text = new StringBuilder("HTTP/1.1 ")
            .append(this.mtd).append(' ')
            .append(uri.getPath())
            .append(" (")
            .append(uri.getHost())
            .append(")\n");
        for (final Map.Entry<String, String> header : this.hdrs) {
            text.append(
                Logger.format(
                    "%s: %s\n",
                    header.getKey(),
                    header.getValue()
                )
            );
        }
        return text.append('\n')
            .append(new RequestBody.Printable(this.content).toString())
            .toString();
    }

    /**
     * Fetch response from server.
     * @param stream The content to send.
     * @return The obtained response
     * @throws IOException If an IO exception occurs.
     */
    private Response fetchResponse(final InputStream stream)
        throws IOException {
        final long start = System.currentTimeMillis();
        final Response response = this.wire.send(
            this, this.home, this.mtd,
            this.hdrs, stream
        );
        final URI uri = URI.create(this.home);
        Logger.info(
            this,
            "#fetch(%s %s%s %s): [%d %s] in %[ms]s",
            this.mtd,
            uri.getHost(),
            // @checkstyle AvoidInlineConditionalsCheck (1 line)
            uri.getPort() > 0 ? String.format(":%d", uri.getPort()) : "",
            uri.getPath(),
            response.status(),
            response.reason(),
            System.currentTimeMillis() - start
        );
        return response;
    }

    /**
     * Base URI.
     */
    @Immutable
    @EqualsAndHashCode(of = "address")
    @Loggable(Loggable.DEBUG)
    private static final class BaseURI implements RequestURI {
        /**
         * URI encapsulated.
         */
        private final transient String address;
        /**
         * Base request encapsulated.
         */
        private final transient BaseRequest owner;
        /**
         * Public ctor.
         * @param req Request
         * @param uri The URI to start with
         */
        BaseURI(final BaseRequest req, final String uri) {
            this.owner = req;
            this.address = uri;
        }
        @Override
        public String toString() {
            return this.address;
        }
        @Override
        public Request back() {
            return new BaseRequest(
                this.owner.wire,
                this.address,
                this.owner.hdrs,
                this.owner.mtd,
                this.owner.content
            );
        }
        @Override
        public URI get() {
            return URI.create(this.owner.home);
        }
        @Override
        public RequestURI set(@NotNull(message = "URI can't be NULL")
            final URI uri) {
            return new BaseRequest.BaseURI(this.owner, uri.toString());
        }
        @Override
        public RequestURI queryParam(
            @NotNull(message = "param name can't be NULL") final String name,
            @NotNull(message = "value can't be NULL") final Object value) {
            return new BaseRequest.BaseURI(
                this.owner,
                UriBuilder.fromUri(this.address)
                    .queryParam(name, "{value}")
                    .build(value).toString()
            );
        }
        @Override
        public RequestURI queryParams(@NotNull(message = "map can't be NULL")
            final Map<String, String> map) {
            final UriBuilder uri = UriBuilder.fromUri(this.address);
            final Object[] values = new Object[map.size()];
            int idx = 0;
            for (final Map.Entry<String, String> pair : map.entrySet()) {
                uri.queryParam(pair.getKey(), String.format("{x%d}", idx));
                values[idx] = pair.getValue();
                ++idx;
            }
            return new BaseRequest.BaseURI(
                this.owner,
                uri.build(values).toString()
            );
        }
        @Override
        public RequestURI path(
            @NotNull(message = "path can't be NULL") final String segment) {
            return new BaseRequest.BaseURI(
                this.owner,
                UriBuilder.fromUri(this.address)
                    .path(segment)
                    .build().toString()
            );
        }
        @Override
        public RequestURI userInfo(
            @NotNull(message = "info can't be NULL") final String info) {
            return new BaseRequest.BaseURI(
                this.owner,
                UriBuilder.fromUri(this.address)
                    .userInfo(info)
                    .build().toString()
            );
        }
        @Override
        public RequestURI port(final int num) {
            return new BaseRequest.BaseURI(
                this.owner,
                UriBuilder.fromUri(this.address)
                    .port(num).build().toString()
            );
        }
    }

    /**
     * Base URI.
     */
    @Immutable
    @EqualsAndHashCode(of = "text")
    @Loggable(Loggable.DEBUG)
    private static final class BaseBody implements RequestBody {
        /**
         * Content encapsulated.
         */
        @Immutable.Array
        private final transient byte[] text;
        /**
         * Base request encapsulated.
         */
        private final transient BaseRequest owner;
        /**
         * Public ctor.
         * @param req Request
         * @param body Text to encapsulate
         */
        BaseBody(final BaseRequest req, final byte[] body) {
            this.owner = req;
            this.text = body.clone();
        }
        @Override
        public String toString() {
            return new RequestBody.Printable(this.text).toString();
        }
        @Override
        public Request back() {
            return new BaseRequest(
                this.owner.wire,
                this.owner.home,
                this.owner.hdrs,
                this.owner.mtd,
                this.text
            );
        }
        @Override
        public String get() {
            return new String(this.text, Constants.CHARSET);
        }
        @Override
        public RequestBody set(@NotNull(message = "content can't be NULL")
            final String txt) {
            return this.set(txt.getBytes(Constants.CHARSET));
        }
        @Override
        public RequestBody set(@NotNull(message = "JSON can't be NULL")
            final JsonStructure json) {
            final StringWriter writer = new StringWriter();
            Json.createWriter(writer).write(json);
            return this.set(writer.toString());
        }
        @Override
        public RequestBody set(@NotNull(message = "body can't be NULL")
            final byte[] txt) {
            return new BaseRequest.BaseBody(this.owner, txt);
        }
        @Override
        public RequestBody formParam(
            @NotNull(message = "name can't be NULL") final String name,
            @NotNull(message = "value can't be NULL") final Object value) {
            try {
                return new BaseRequest.BaseBody(
                    this.owner,
                    new StringBuilder(this.get())
                        .append(name)
                        .append('=')
                        .append(
                            URLEncoder.encode(
                                value.toString(),
                                Constants.ENCODING
                            )
                        )
                        .append('&')
                        .toString()
                        .getBytes(Constants.CHARSET)
                );
            } catch (final UnsupportedEncodingException ex) {
                throw new IllegalStateException(ex);
            }
        }
        @Override
        public RequestBody formParams(
            @NotNull(message = "map of params can't be NULL")
            final Map<String, String> params) {
            RequestBody body = this;
            for (final Map.Entry<String, String> param : params.entrySet()) {
                body = body.formParam(param.getKey(), param.getValue());
            }
            return body;
        }
    }

}
