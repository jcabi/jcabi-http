/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire that caches GET requests.
 *
 * <p>This decorator can be used when you want to avoid duplicate
 * GET requests to load-sensitive resources, for example:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(FileCachingWire.class)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>You can also configure it to flush the entire cache
 * on certain request URI's, for example:
 *
 * <pre>new JdkRequest(uri)
 *   .through(CachingWire.class, "GET /save/.*")
 *   .uri().path("/save/123").back()
 *   .fetch();</pre>
 *
 * <p>The regular expression provided will be used against a string
 * constructed as an HTTP method, space, path of the URI together with
 * query part.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 1.16
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin", "regex" })
public final class FcWire implements Wire {

    /**
     * Cache in files.
     */
    private final transient FcCache cache;

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Flushing regular expression.
     */
    private final transient String regex;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public FcWire(final Wire wire) {
        this(wire, "$never");
    }

    /**
     * Public ctor.
     * @param wire Original wire
     * @param flsh Flushing regular expression
     */
    public FcWire(final Wire wire, final String flsh) {
        this(wire, flsh, new FcCache());
    }

    /**
     * Public ctor.
     * @param wire Original wire
     * @param flsh Flushing regular expression
     * @param path Path for the files
     */
    public FcWire(final Wire wire, final String flsh, final String path) {
        this(wire, flsh, new FcCache(path));
    }

    /**
     * Public ctor.
     * @param wire Original wire
     * @param flsh Flushing regular expression
     * @param fcc Cache
     */
    public FcWire(final Wire wire, final String flsh, final FcCache fcc) {
        this.origin = wire;
        this.regex = flsh;
        this.cache = fcc;
    }

    // @checkstyle ParameterNumber (5 lines)
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read) throws IOException {
        final URI uri = req.uri().get();
        final StringBuilder label = new StringBuilder(100)
            .append(method).append(' ').append(uri.getPath());
        if (uri.getQuery() != null) {
            label.append('?').append(uri.getQuery());
        }
        if (label.toString().matches(this.regex)) {
            this.cache.invalidate();
        }
        final Response rsp;
        if (method.equals(Request.GET)) {
            rsp = this.cache.get(
                label.toString(), this.origin, req,
                home, method, headers, content, connect, read
            );
        } else {
            rsp = this.origin.send(
                req, home, method, headers, content,
                connect, read
            );
        }
        return rsp;
    }

}
