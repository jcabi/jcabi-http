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

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is the base class to handle http responses with 304 state.
 *
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

    // @checkstyle ParameterNumber (3 lines)
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

    /**
     * Check cache and update if needed.
     *
     * @param req Request
     * @param home URI to fetch
     * @param method HTTP method
     * @param headers Headers
     * @param content HTTP body
     * @param connect The connect timeout
     * @param read The read timeout
     * @return Response obtained
     * @throws IOException if fails
     * @checkstyle ParameterNumber (6 lines)
     */
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

    /**
     * Check response and update cache or evict from cache if needed.
     * @param req Request
     * @param home URI to fetch
     * @param method HTTP method
     * @param headers Headers
     * @param content HTTP body
     * @param connect The connect timeout
     * @param read The read timeout
     * @return Response obtained
     * @throws IOException if fails
     * @checkstyle ParameterNumber (8 lines)
     */
    private Response validateCacheWithServer(
        final Request req, final String home, final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content, final int connect, final int read
    ) throws IOException {
        final Response cached = this.cache.get(req);
        final Collection<Map.Entry<String, String>> hdrs = this.enrich(
            headers, cached
        );
        Response result = this.origin.send(
            req, home, method, hdrs, content, connect, read
        );
        if (result.status() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            result = cached;
        } else {
            this.updateCache(req, result);
        }
        return result;
    }

    /**
     * Add, update or evict response in cache.
     * @param req The request to be used as key
     * @param rsp The response to add/update
     */
    private void updateCache(final Request req, final Response rsp) {
        if (rsp.headers().containsKey(this.scvh)) {
            this.cache.put(req, rsp);
        } else if (rsp.status() == HttpURLConnection.HTTP_OK) {
            this.cache.remove(req);
        }
    }

    /**
     * Add identify content version header.
     *
     * @param headers Original headers
     * @param rsp Cached response
     * @return Map with extra header
     */
    private Collection<Map.Entry<String, String>> enrich(
        final Collection<Map.Entry<String, String>> headers, final Response rsp
    ) {
        final Collection<String> list = rsp.headers().get(
            this.scvh
        );
        final Map<String, String> map =
            new ConcurrentHashMap<>(headers.size() + 1);
        for (final Map.Entry<String, String> entry : headers) {
            map.put(entry.getKey(), entry.getValue());
        }
        map.put(
            this.cmch, list.iterator().next()
        );
        return map.entrySet();
    }

    /**
     * Check if the request send through this Wire has the cmch header.
     * @param headers The headers of the request.
     * @return True if the request contains the cmch header, false otherwise.
     */
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
