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
import com.jcabi.log.Logger;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire with HTTP basic authentication based on user info of URI.
 *
 * <p>This wire converts user info from URI into
 * {@code "Authorization"} HTTP header, for example:
 *
 * <pre> String html = new JdkRequest("http://jeff:12345@example.com")
 *   .through(BasicAuthWire.class)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>In this example, an additional HTTP header {@code Authorization}
 * will be added with a value {@code Basic amVmZjoxMjM0NQ==}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @see <a href="http://tools.ietf.org/html/rfc2617">RFC 2617 "HTTP Authentication: Basic and Digest Access Authentication"</a>
 * @since 0.10
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class BasicAuthWire implements Wire {

    /**
     * The Charset to use.
     */
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public BasicAuthWire(final Wire wire) {
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
        boolean absent = true;
        for (final Map.Entry<String, String> header : headers) {
            if (header.getKey().equals(HttpHeaders.AUTHORIZATION)) {
                Logger.warn(
                    this,
                    "Request already contains %s header",
                    HttpHeaders.AUTHORIZATION
                );
                absent = false;
            }
            hdrs.add(header);
        }
        final String info = URI.create(home).getUserInfo();
        if (absent && info != null) {
            final String[] parts = info.split(":", 2);
            hdrs.add(
                new ImmutableHeader(
                    HttpHeaders.AUTHORIZATION,
                    Logger.format(
                        "Basic %s",
                        DatatypeConverter.printBase64Binary(
                            Logger.format(
                                "%s:%s",
                                parts[0],
                                parts[1]
                            ).getBytes(BasicAuthWire.CHARSET)
                        )
                    )
                )
            );
        }
        return this.origin.send(
            req.uri().userInfo(null).back(),
            home,
            method,
            hdrs,
            content,
            connect,
            read
        );
    }
}
