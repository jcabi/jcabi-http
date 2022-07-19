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
        final InputStream input = req.getInputStream();
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        while (true) {
            final int bytes = input.read(buffer);
            if (bytes == -1) {
                break;
            }
            output.write(buffer, 0, bytes);
        }
        return output.toByteArray();
    }
}
