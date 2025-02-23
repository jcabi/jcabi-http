/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
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
 * @see <a href="http://tools.ietf.org/html/rfc2965">RFC 2965 "HTTP State Management Mechanism"</a>
 * @since 0.10
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
        headers.forEach(
            header -> {
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
            });
        if (!cookies.isEmpty()) {
            final String text = cookies.entrySet().stream().filter(
                cookie -> !cookie.getValue().isEmpty()
            ).map(
                cookie -> cookie.getKey() + '=' + cookie.getValue()
            ).collect(
                Collectors.joining("; ")
            );
            hdrs.add(
                new ImmutableHeader(
                    HttpHeaders.COOKIE,
                        text
                )
            );
        }
        return this.origin.send(
            req, home, method, hdrs, content, connect, read
        );
    }
}
