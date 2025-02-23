/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Wire;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.ToString;

/**
 * Wire that caches requests with ETags.
 *
 * <p>This decorator can be used when you want to avoid duplicate
 * requests to load-sensitive resources and server supports ETags, for example:
 *
 * <pre>{@code
 *    String html = new JdkRequest("http://goggle.com")
 *        .through(ETagCachingWire.class)
 *        .fetch()
 *        .body();
 * }</pre>
 *
 * <p>Client will automatically detect if server uses ETags and start adding
 * corresponding If-None-Match to outgoing requests
 *
 * <p>Client will take response from the cache if it is present
 * or will query resource for that.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 2.0
 */
@ToString
@Immutable
public final class ETagCachingWire extends AbstractHeaderBasedCachingWire {

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public ETagCachingWire(final Wire wire) {
        super(HttpHeaders.ETAG, HttpHeaders.IF_NONE_MATCH, wire);
    }
}
