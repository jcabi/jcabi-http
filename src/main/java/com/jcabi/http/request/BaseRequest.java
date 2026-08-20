/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.fasterxml.jackson.databind.util.ClassUtil;
import com.google.common.base.Stopwatch;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import com.jcabi.http.RequestURI;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import jakarta.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;

/**
 * Base implementation of {@link Request}.
 * @see Request
 * @see Response
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(of = {"home", "mtd", "hdrs", "content"})
@Loggable(Loggable.DEBUG)
// @todo #87:30min Refactor this class to get rid of PMD.GodClass.
//  This can be done if MultiPartFormBody and
//  FormEncodedBody are pulled out. Also, the two
//  share the same implementations for all methods besides formParam,
//  so they can be refactored to extend an AbstractRequestBody.
//  PMD.TooManyMethods might come together with getting rid of the
//  first one, since maybe qulice is counting the methods in the inner
//  classes too - if it doesn't, then it can be left.
public final class BaseRequest implements Request {

    /**
     * An empty immutable {@code byte} array.
     */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

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
     * Socket timeout to use.
     */
    private final transient int connect;

    /**
     * Read timeout to use.
     */
    private final transient int read;

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
    public BaseRequest(final Wire wre, final String uri) {
        this(
            wre, uri,
            new Array<Map.Entry<String, String>>(),
            Request.GET, BaseRequest.EMPTY_BYTE_ARRAY
        );
    }

    /**
     * Public ctor.
     * @param wre Wire
     * @param uri The resource to work with
     * @param headers Headers
     * @param method HTTP method
     * @param body HTTP request body
     */
    public BaseRequest(
        final Wire wre, final String uri,
        final Iterable<Map.Entry<String, String>> headers,
        final String method, final byte[] body
    ) {
        this(wre, uri, headers, method, body, 0, 0);
    }

    /**
     * Public ctor.
     * @param wre Wire
     * @param uri The resource to work with
     * @param headers Headers
     * @param method HTTP method
     * @param body HTTP request body
     * @param cnct Connect timeout for http connection
     * @param rdd Read timeout for http connection
     */
    public BaseRequest(
        final Wire wre, final String uri,
        final Iterable<Map.Entry<String, String>> headers,
        final String method, final byte[] body,
        final int cnct, final int rdd
    ) {
        this(
            wre, headers, BaseRequest.createUri(uri).toString(),
            method, body, cnct, rdd
        );
    }

    /**
     * Private ctor.
     * @param wre Wire
     * @param headers Headers
     * @param uri The resource to work with
     * @param method HTTP method
     * @param body HTTP request body
     * @param cnct Connect timeout for http connection
     * @param rdd Read timeout for http connection
     */
    private BaseRequest(
        final Wire wre,
        final Iterable<Map.Entry<String, String>> headers, final String uri,
        final String method, final byte[] body,
        final int cnct, final int rdd
    ) {
        this.wire = wre;
        this.home = uri;
        this.hdrs = new Array<>(headers);
        this.mtd = method;
        this.content = body.clone();
        this.connect = cnct;
        this.read = rdd;
    }

    @Override
    public RequestURI uri() {
        return new BaseUri(this, this.home);
    }

    @Override
    public Request header(final String name, final Object value) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs.with(new ImmutableHeader(name, value.toString())),
            this.mtd,
            this.content,
            this.connect,
            this.read
        );
    }

    @Override
    public Request reset(final String name) {
        final Collection<Map.Entry<String, String>> headers =
            new ArrayList<>(0);
        for (final Map.Entry<String, String> header : this.hdrs) {
            if (!header.getKey().equals(ImmutableHeader.normalize(name))) {
                headers.add(header);
            }
        }
        return new BaseRequest(
            this.wire,
            this.home,
            headers,
            this.mtd,
            this.content,
            this.connect,
            this.read
        );
    }

    @Override
    public RequestBody body() {
        return new FormEncodedBody(this, this.content);
    }

    @Override
    public RequestBody multipartBody() {
        return new MultipartFormBody(this, this.content);
    }

    @Override
    public Request method(final String method) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs,
            method,
            this.content,
            this.connect,
            this.read
        );
    }

    @Override
    public Request timeout(final int cnct, final int rdd) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs,
            this.mtd,
            this.content,
            cnct,
            rdd
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
    public <T extends Wire> Request through(
        final Class<T> type,
        final Object... args
    ) {
        return this.through(this.mkWire(type, args));
    }

    @Override
    public Request through(final Wire wre) {
        return new BaseRequest(
            wre,
            this.home,
            this.hdrs,
            this.mtd,
            this.content,
            this.connect,
            this.read
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
            .append(')').append(System.lineSeparator());
        for (final Map.Entry<String, String> header : this.hdrs) {
            text.append(
                Logger.format(
                    "%s: %s%n",
                    header.getKey(),
                    header.getValue()
                )
            );
        }
        return text.append('\n')
            .append(new RequestBody.Printable(this.content).toString())
            .toString();
    }

    String home() {
        return this.home;
    }

    Iterable<Map.Entry<String, String>> headers() {
        return this.hdrs;
    }

    Request uri(final String address) {
        return new BaseRequest(
            this.wire,
            address,
            this.hdrs,
            this.mtd,
            this.content,
            this.connect,
            this.read
        );
    }

    Request body(final byte[] text) {
        return new BaseRequest(
            this.wire,
            this.home,
            this.hdrs,
            this.mtd,
            text,
            this.connect,
            this.read
        );
    }

    private <T extends Wire> Wire mkWire(
        final Class<T> type,
        final Object... args
    ) {
        final Object[] params = new Object[args.length + 1];
        params[0] = this.wire;
        System.arraycopy(args, 0, params, 1, args.length);
        final Wire decorated;
        try {
            decorated = Wire.class.cast(
                BaseRequest.findCtor(type, args).newInstance(params)
            );
        } catch (final InstantiationException
            | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        }
        return decorated;
    }

    private Response fetchResponse(final InputStream stream)
        throws IOException {
        final Stopwatch watch = Stopwatch.createStarted();
        final Response response = this.wire.send(
            this, this.home, this.mtd,
            this.hdrs, stream, this.connect,
            this.read
        );
        watch.stop();
        final URI uri = URI.create(this.home);
        if (Logger.isInfoEnabled(this)) {
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
                watch.elapsed(TimeUnit.MILLISECONDS)
            );
        }
        return response;
    }

    private static URI createUri(final String uri) {
        URI addr = URI.create(uri);
        if (addr.getPath() != null && addr.getPath().isEmpty()) {
            addr = UriBuilder.fromUri(addr).path("/").build();
        }
        return addr;
    }

    private static <T extends Wire> Constructor<?> findCtor(
        final Class<T> type, final Object... args
    ) {
        Constructor<?> ctor = null;
        for (final Constructor<?> opt : type.getDeclaredConstructors()) {
            final Class<?>[] types = opt.getParameterTypes();
            if (types.length == args.length + 1) {
                boolean match = true;
                for (int inx = 1; inx < types.length && match; ++inx) {
                    match = BaseRequest
                        .wrappedIfNeeded(types[inx])
                        .isAssignableFrom(args[inx - 1].getClass());
                }
                if (match) {
                    ctor = opt;
                    break;
                }
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
        return ctor;
    }

    private static Class<?> wrappedIfNeeded(final Class<?> type) {
        Class<?> arg = type;
        if (arg.isPrimitive()) {
            arg = ClassUtil.wrapperType(arg);
        }
        return arg;
    }
}
