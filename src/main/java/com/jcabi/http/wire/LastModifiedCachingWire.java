/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Wire;
import jakarta.ws.rs.core.HttpHeaders;
import lombok.ToString;

/**
 * Wire that caches requests based on Last-Modified
 * and If-Modified-Since headers.
 * @since 1.15
 */
@ToString
public final class LastModifiedCachingWire
    extends AbstractHeaderBasedCachingWire {

    /**
     * Public ctor.
     * @param origin Original wire
     */
    public LastModifiedCachingWire(final Wire origin) {
        super(HttpHeaders.LAST_MODIFIED, HttpHeaders.IF_MODIFIED_SINCE, origin);
    }
}
