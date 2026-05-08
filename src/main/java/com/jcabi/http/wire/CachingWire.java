/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
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
 * @since 1.0
 * @todo #179:30m This implementation depends on Guava. Investigate for a
 *  possible shared interface between this class and other implementations for
 *  caching. If this shared interface is possible replace this task with a task
 *  for implementing it.
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
     * Pragma HTTP header name (RFC 7234 §5.4).
     */
    private static final String PRAGMA = "Pragma";

    /**
     * No-cache directive value used in {@code Cache-Control}
     * and {@code Pragma} request headers.
     */
    private static final String NO_CACHE = "no-cache";

    /**
     * No-store directive value used in {@code Cache-Control}
     * request headers.
     */
    private static final String NO_STORE = "no-store";

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
        final StringBuilder label = new StringBuilder(100)
            .append(method).append(' ').append(uri.getPath());
        if (uri.getQuery() != null) {
            label.append('?').append(uri.getQuery());
        }
        if (label.toString().matches(this.regex)) {
            this.cache.invalidateAll();
        }
        final Response rsp;
        if (method.equals(Request.GET) && !CachingWire.bypass(headers)) {
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
     * Should the cache be bypassed for this request?
     *
     * <p>Per RFC 7234 §5.2.1, a request that carries
     * {@code Cache-Control: no-cache} or {@code no-store} must not be
     * served from a cached response, and a {@code no-store} request must
     * not be stored either. {@code Pragma: no-cache} is the HTTP/1.0
     * backwards-compatible equivalent of {@code Cache-Control: no-cache}
     * (RFC 7234 §5.4).
     *
     * @param headers Request headers
     * @return Whether the request asks the cache to be skipped
     */
    private static boolean bypass(
        final Collection<Map.Entry<String, String>> headers
    ) {
        boolean bypass = false;
        for (final Map.Entry<String, String> header : headers) {
            final String name = header.getKey();
            final String value = header.getValue()
                .toLowerCase(Locale.ENGLISH);
            if (HttpHeaders.CACHE_CONTROL.equals(name)
                && (value.contains(CachingWire.NO_CACHE)
                || value.contains(CachingWire.NO_STORE))) {
                bypass = true;
                break;
            }
            if (CachingWire.PRAGMA.equals(name)
                && value.contains(CachingWire.NO_CACHE)) {
                bypass = true;
                break;
            }
        }
        return bypass;
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
