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
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire that compresses cookies before sending.
 *
 * <p>This wire compresses all provided {@code Cookie} headers into one
 * and removes empty cookies, for example:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(CookieOptimizingWire.class)
 *   .header(HttpHeaders.Cookie, "alpha=test")
 *   .header(HttpHeaders.Cookie, "beta=")
 *   .header(HttpHeaders.Cookie, "gamma=foo")
 *   .fetch()
 *   .body();</pre>
 *
 * <p>An actual HTTP request will be sent with just one {@code Cookie}
 * header with a value {@code alpha=test; gamma=foo}.
 *
 * <p>It is highly recommended to use this wire decorator when you're
 * working with cookies.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.10
 * @see <a href="http://tools.ietf.org/html/rfc2965">RFC 2965 "HTTP State Management Mechanism"</a>
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class CookieOptimizingWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public CookieOptimizingWire(final Wire wire) {
        this.origin = wire;
    }

    // @checkstyle ParameterNumber (7 lines)
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read) throws IOException {
        final Collection<Map.Entry<String, String>> hdrs =
            new LinkedList<>();
        final ConcurrentMap<String, String> cookies =
            new ConcurrentHashMap<>(0);
        for (final Map.Entry<String, String> header : headers) {
            if (header.getKey().equals(HttpHeaders.COOKIE)) {
                final String cookie = header.getValue();
                final int split = cookie.indexOf('=');
                final String name = cookie.substring(0, split);
                final String value = cookie.substring(split + 1);
                if (value.isEmpty()) {
                    cookies.remove(name);
                } else {
                    cookies.put(name, value);
                }
            } else {
                hdrs.add(header);
            }
        }
        if (!cookies.isEmpty()) {
            final StringBuilder text = new StringBuilder(0);
            for (final Map.Entry<String, String> cookie : cookies.entrySet()) {
                if (cookie.getValue().isEmpty()) {
                    continue;
                }
                if (text.length() > 0) {
                    text.append("; ");
                }
                text.append(cookie.getKey())
                    .append('=')
                    .append(cookie.getValue());
            }
            hdrs.add(
                new ImmutableHeader(
                    HttpHeaders.COOKIE,
                    text.toString()
                )
            );
        }
        return this.origin.send(
            req, home, method, hdrs, content, connect, read
        );
    }
}
