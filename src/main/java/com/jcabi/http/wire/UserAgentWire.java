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
import com.jcabi.manifests.Manifests;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wire with default user agent.
 *
 * <p>This wire adds an extra HTTP header {@code User-Agent} to the request,
 * if it's not yet provided, for example:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(UserAgentWire.class)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>An actual HTTP request will be sent with {@code User-Agent}
 * header with a value {@code ReXSL-0.1/abcdef0 Java/1.6} (for example). It
 * is recommended to use this wire decorator when you're working with
 * third party RESTful services, to properly identify yourself and avoid
 * troubles.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.43">RFC 2616 section 14.43 "User-Agent"</a>
 * @since 0.10
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
@RequiredArgsConstructor
public final class UserAgentWire implements Wire {

    /**
     * Original wire.
     */
    private final Wire origin;

    /**
     * Agent.
     */
    private final String agent;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public UserAgentWire(final Wire wire) {
        this(
            wire,
            String.format(
                "jcabi-%s/%s Java/%s",
                Manifests.read("JCabi-Version"),
                Manifests.read("JCabi-Build"),
                System.getProperty("java.version")
            )
        );
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
        boolean absent = true;
        for (final Map.Entry<String, String> header : headers) {
            hdrs.add(header);
            if (header.getKey().equals(HttpHeaders.USER_AGENT)) {
                absent = false;
            }
        }
        if (absent) {
            hdrs.add(
                new ImmutableHeader(
                    HttpHeaders.USER_AGENT,
                    this.agent
                )
            );
        }
        return this.origin.send(
            req, home, method, hdrs, content, connect, read
        );
    }
}
