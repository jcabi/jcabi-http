/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.immutable.ArrayMap;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.Request;

/**
 * Mock HTTP query/request.
 *
 * @since 0.10
 */
@Immutable
final class GrizzlyQuery implements MkQuery {

    /**
     * HTTP request method.
     */
    private final transient Method mtd;

    /**
     * HTTP request content.
     */
    @Immutable.Array
    private final transient byte[] content;

    /**
     * HTTP request URI.
     */
    private final transient String home;

    /**
     * HTTP request headers.
     */
    private final transient ArrayMap<String, List<String>> hdrs;

    /**
     * Ctor.
     * @param request Grizzly request
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.ConstructorOnlyInitializesOrCallOtherConstructors")
    GrizzlyQuery(final Request request) throws IOException {
        request.setCharacterEncoding(StandardCharsets.UTF_8.toString());
        this.home = GrizzlyQuery.uri(request);
        this.mtd = request.getMethod();
        this.hdrs = GrizzlyQuery.headers(request);
        this.content = GrizzlyQuery.input(request);
    }

    @Override
    public URI uri() {
        return URI.create(this.home);
    }

    @Override
    public String method() {
        return this.mtd.getMethodString();
    }

    @Override
    public Map<String, List<String>> headers() {
        return Collections.unmodifiableMap(this.hdrs);
    }

    @Override
    public String body() {
        return new String(this.content, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] binary() {
        return Arrays.copyOf(this.content, this.content.length);
    }

    /**
     * Fetch URI from the request.
     * @param request Request
     * @return URI
     */
    private static String uri(final Request request) {
        final StringBuilder uri = new StringBuilder(request.getRequestURI());
        final String query = request.getQueryString();
        if (query != null && !query.isEmpty()) {
            uri.append('?').append(query);
        }
        return uri.toString();
    }

    /**
     * Fetch headers from the request.
     * @param request Request
     * @return Headers
     */
    private static ArrayMap<String, List<String>> headers(
        final Request request
    ) {
        final ConcurrentMap<String, List<String>> headers =
            new ConcurrentHashMap<>(0);
        final Iterable<String> names = request.getHeaderNames();
        for (final String name : names) {
            headers.put(
                ImmutableHeader.normalize(name),
                GrizzlyQuery.headers(request, name)
            );
        }
        return new ArrayMap<>(headers);
    }

    /**
     * Get headers by name.
     * @param request Grizzly request
     * @param name Name of header
     * @return List of values
     */
    private static List<String> headers(
        final Request request, final String name
    ) {
        final List<String> list = new LinkedList<>();
        final Iterable<?> values = request.getHeaders(name);
        for (final Object value : values) {
            list.add(value.toString());
        }
        return list;
    }

    /**
     * Read req.
     * @param req Grizzly req
     * @return Bytes of input
     * @throws IOException If fails
     */
    private static byte[] input(final Request req) throws IOException {
        // @checkstyle MagicNumber (1 line)
        final byte[] buffer = new byte[8192];
        final ByteArrayOutputStream output;
        try (InputStream input = req.getInputStream()) {
            output = new ByteArrayOutputStream();
            while (true) {
                final int bytes = input.read(buffer);
                if (bytes == -1) {
                    break;
                }
                output.write(buffer, 0, bytes);
            }
        }
        return output.toByteArray();
    }
}
