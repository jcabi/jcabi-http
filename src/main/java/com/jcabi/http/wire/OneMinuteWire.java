/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Timeable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire that throws an {@link IOException} if a request takes longer
 * than a minute.
 *
 * <p>It's recommended to use this decorator in production, in order
 * to avoid stuck requests:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(OneMinuteWire.class)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.10
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class OneMinuteWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public OneMinuteWire(final Wire wire) {
        this.origin = wire;
    }

    // @checkstyle ParameterNumber (5 lines)
    @Override
    @Timeable(limit = 1, unit = TimeUnit.MINUTES)
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read) throws IOException {
        return this.origin.send(
            req, home, method, headers, content, connect, read
        );
    }
}
