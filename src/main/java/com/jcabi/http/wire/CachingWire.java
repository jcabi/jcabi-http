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
package com.jcabi.http.wire;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire that caches GET requests.
 *
 * <p>This decorator can be used when you want to avoid duplicate
 * GET requests to load-sensitive resources, for example:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(CachingWire.class)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>Since 1.5, you can also configure it to flush the entire cache
 * on certain request URI's, for example:
 *
 * <pre>new JdkRequest(uri)
 *   .through(CachingWire.class, "GET /save/.*")
 *   .uri().path("/save/123").back()
 *   .fetch();</pre>
 *
 * <p>Since 1.17.3, you can pass a {@see LoadingCache} alongside the wire.
 *
 * <pre>{@code
 * final LoadingCache<Callable<Response>, Response> cache = ...;
 * new JdkRequest(uri)
 *   .through(CachingWire.class, cache)
 *   .uri().path("/save/123").back()
 *   .fetch();
 *  }</pre>
 *
 * <p>The regular expression provided will be used against a string
 * constructed as an HTTP method, space, path of the URI together with
 * query part.
 *
 * <p>The class is immutable and thread-safe.
 * @todo #179:30m This implementation depends on Guava. Investigate for a
 *  possible shared interface between this class and other implementations for
 *  caching. If this shared interface is possible replace this task with a task
 *  for implementing it.
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = {"origin", "regex"})
@SuppressWarnings("PMD.OnlyOneConstructorShouldDoInitialization")
public final class CachingWire implements Wire {

    /**
     * Loader.
     */
    private static final CacheLoader<Wire,
        LoadingCache<Callable<Response>, Response>> LOADER =
        new CacheLoader<Wire, LoadingCache<Callable<Response>, Response>>() {
            @Override
            public LoadingCache<Callable<Response>, Response> load(
                final Wire key
            ) {
                return CacheBuilder.newBuilder().build(
                    new CacheLoader<Callable<Response>, Response>() {
                        @Override
                        public Response load(final Callable<Response> query)
                            throws Exception {
                            return query.call();
                        }
                    }
                );
            }
        };

    /**
     * Default cache.
     */
    private static final LoadingCache<Wire,
        LoadingCache<Callable<Response>, Response>> CACHE =
        CacheBuilder.newBuilder().build(CachingWire.LOADER);

    /**
     * Default flushing regex.
     */
    private static final String NEVER = "$never";

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Flushing regular expression.
     */
    private final transient String regex;

    /**
     * Cache.
     */
    private final LoadingCache<Callable<Response>, Response> cache;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public CachingWire(final Wire wire) {
        this(wire, CachingWire.NEVER);
    }

    /**
     * Public ctor.
     * @param wire Original wire
     * @param flsh Flushing regular expression
     * @since 1.5
     */
    public CachingWire(final Wire wire, final String flsh) {
        this.origin = wire;
        this.regex = flsh;
        this.cache = CACHE.getUnchecked(this);
    }

    /**
     * Public ctor.
     * @param wire Original wire
     * @param storage Cache
     * @since 1.17.4
     */
    public CachingWire(
        final Wire wire,
        final LoadingCache<Callable<Response>, Response> storage
    ) {
        this(wire, CachingWire.NEVER, storage);
    }

    /**
     * Public ctor.
     * @param wire Original wire
     * @param flsh Flushing regular expression
     * @param storage Cache
     * @since 1.17.4
     */
    public CachingWire(
        final Wire wire,
        final String flsh,
        final LoadingCache<Callable<Response>, Response> storage
    ) {
        this.origin = wire;
        this.regex = flsh;
        this.cache = storage;
    }

    // @checkstyle ParameterNumber (5 lines)
    @Override
    public Response send(
        final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read
    ) throws IOException {
        final URI uri = req.uri().get();
        final StringBuilder label = new StringBuilder(Tv.HUNDRED)
            .append(method).append(' ').append(uri.getPath());
        if (uri.getQuery() != null) {
            label.append('?').append(uri.getQuery());
        }
        if (label.toString().matches(this.regex)) {
            this.cache.invalidateAll();
        }
        final Response rsp;
        if (method.equals(Request.GET)) {
            try {
                rsp = this.cache.get(
                    new CachingWire.Query(
                        this.origin, req, home, headers, content,
                        connect, read
                    )
                );
            } catch (final ExecutionException ex) {
                throw new IOException(ex);
            }
        } else {
            rsp = this.origin.send(
                req, home, method, headers, content,
                connect, read
            );
        }
        return rsp;
    }

    /**
     * Invalidate the entire cache.
     * @since 1.15
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static void invalidate() {
        CachingWire.CACHE.invalidateAll();
    }

    /**
     * Query.
     *
     * @since 1.8.3
     */
    @ToString
    @EqualsAndHashCode(of = {"origin", "request", "uri", "headers"})
    private static final class Query implements Callable<Response> {
        /**
         * Origin wire.
         */
        private final transient Wire origin;

        /**
         * Request.
         */
        private final transient Request request;

        /**
         * URI.
         */
        private final transient String uri;

        /**
         * Headers.
         */
        private final transient Collection<Map.Entry<String, String>> headers;

        /**
         * Body.
         */
        private final transient InputStream body;

        /**
         * Connect timeout.
         */
        private final transient int connect;

        /**
         * Read timeout.
         */
        private final transient int read;

        /**
         * Ctor.
         * @param wire Original wire
         * @param req Request
         * @param home URI to fetch
         * @param hdrs Headers
         * @param input Input body
         * @param cnct Connect timeout
         * @param rdd Read timeout
         * @checkstyle ParameterNumberCheck (5 lines)
         */
        Query(
            final Wire wire, final Request req, final String home,
            final Collection<Map.Entry<String, String>> hdrs,
            final InputStream input, final int cnct,
            final int rdd
        ) {
            this.origin = wire;
            this.request = req;
            this.uri = home;
            this.headers = hdrs;
            this.body = input;
            this.connect = cnct;
            this.read = rdd;
        }

        @Override
        public Response call() throws IOException {
            return this.origin.send(
                this.request, this.uri, Request.GET, this.headers, this.body,
                this.connect, this.read
            );
        }
    }

}
