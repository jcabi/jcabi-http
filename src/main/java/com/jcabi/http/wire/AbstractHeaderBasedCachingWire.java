/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the base class to handle http responses with 304 state.
 * @since 2.0
 */
public abstract class AbstractHeaderBasedCachingWire implements Wire {

    /**
     * Cache.
     */
    private final transient Map<Request, Response> cache;

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * This header will be return by server to identify content version.
     */
    private final transient String scvh;

    /**
     * This header will be sent by client to check if server content has
     * been changed.
     */
    private final transient String cmch;

    /**
     * Ctor.
     * @param scvh Server Response Version Header name
     * @param cmch Client Modification Check Header name
     * @param wire Original wire
     */
    AbstractHeaderBasedCachingWire(
        final String scvh, final String cmch, final Wire wire
    ) {
        this.scvh = scvh;
        this.cmch = cmch;
        this.origin = wire;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public final Response send(
        final Request req, final String home, final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content, final int connect, final int read
    ) throws IOException {
        final Response rsp;
        if (method.equals(Request.GET) && !this.requestHasCmcHeader(headers)) {
            rsp = this.consultCache(
                req, home, method, headers, content, connect, read
            );
        } else {
            rsp = this.origin.send(
                req, home, method, headers, content, connect, read
            );
        }
        return rsp;
    }

    private Response consultCache(
        final Request req, final String home, final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content, final int connect, final int read
    ) throws IOException {
        final Response rsp;
        if (this.cache.containsKey(req)) {
            rsp = this.validateCacheWithServer(
                req, home, method, headers, content, connect, read
            );
        } else {
            rsp = this.origin.send(
                req, home, method, headers, content, connect, read
            );
            this.updateCache(req, rsp);
        }
        return rsp;
    }

    private Response validateCacheWithServer(
        final Request req, final String home, final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content, final int connect, final int read
    ) throws IOException {
        final Response cached = this.cache.get(req);
        Response result = this.origin.send(
            req, home, method, this.enrich(headers, cached),
            content, connect, read
        );
        if (result.status() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            result = cached;
        } else {
            this.updateCache(req, result);
        }
        return result;
    }

    private void updateCache(final Request req, final Response rsp) {
        if (rsp.headers().containsKey(this.scvh)) {
            this.cache.put(req, rsp);
        } else if (rsp.status() == HttpURLConnection.HTTP_OK) {
            this.cache.remove(req);
        }
    }

    private Collection<Map.Entry<String, String>> enrich(
        final Collection<Map.Entry<String, String>> headers, final Response rsp
    ) {
        final Map<String, String> map =
            new HashMap<>(headers.size() + 1);
        for (final Map.Entry<String, String> entry : headers) {
            map.put(entry.getKey(), entry.getValue());
        }
        map.put(
            this.cmch, rsp.headers().get(this.scvh).iterator().next()
        );
        return map.entrySet();
    }

    private boolean requestHasCmcHeader(
        final Collection<Map.Entry<String, String>> headers
    ) {
        boolean result = false;
        for (final Map.Entry<String, String> header : headers) {
            if (header.getKey().equals(this.cmch)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
