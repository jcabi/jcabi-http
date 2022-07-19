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
        final StringBuilder label = new StringBuilder(Tv.HUNDRED)
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
