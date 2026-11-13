/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.log.Logger;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire with HTTP bearer token authentication.
 *
 * <p>This wire adds an {@code "Authorization: Bearer ..."} HTTP header
 * to the request, if it's not yet provided, for example:</p>
 *
 * <pre> String html = new JdkRequest("http://my.example.com")
 *   .through(BearerAuthWire.class, "mF_9.B5f-4.1JqM")
 *   .fetch()
 *   .body();</pre>
 *
 * <p>In this example, an additional HTTP header {@code Authorization}
 * will be added with a value {@code Bearer mF_9.B5f-4.1JqM}.</p>
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @see <a href="http://tools.ietf.org/html/rfc6750">RFC 6750 "The OAuth 2.0 Authorization Framework: Bearer Token Usage"</a>
 * @since 2.0
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class BearerAuthWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * The bearer token to use.
     */
    private final transient String token;

    /**
     * Public ctor.
     *
     * @param origin Original wire
     * @param token  Bearer token
     */
    public BearerAuthWire(final Wire origin, final String token) {
        this.origin = origin;
        this.token = token;
    }

    @Override
    public Response send(
        final Request req,
        final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read
    ) throws IOException {
        final Collection<Map.Entry<String, String>> hdrs =
            new ArrayList<>(headers);
        if (
            headers.stream().noneMatch(
                h -> h.getKey().equalsIgnoreCase(HttpHeaders.AUTHORIZATION)
            )
        ) {
            hdrs.add(
                new ImmutableHeader(
                    HttpHeaders.AUTHORIZATION,
                    String.format("Bearer %s", this.token)
                )
            );
        } else {
            Logger.warn(
                this,
                "Request already contains %s header",
                HttpHeaders.AUTHORIZATION
            );
        }
        return this.origin.send(req, home, method, hdrs, content, connect, read);
    }
}
