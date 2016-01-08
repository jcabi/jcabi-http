/**
 * Copyright (c) 2011-2015, jcabi.com
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

import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wire that caches requests based on (for five minutes).
 * @author Igor Piddubnyi (igor.piddubnyi@gmail.com)
 * @version $Id$
 * @since 1.0
 */
public class LastModifiedCashingWire implements Wire {

    /**
     * Last-Modified header name.
     */
    public static final String HEADER_LAST_MODIFIED = "Last-Modified";

    /**
     * If-Modified-Since header name.
     */
    public static final String HEADER_IF_MODIFIED_SINCE = "If-Modified-Since";
    /**
     * Cache.
     */
    private static final Map<String, Response> CACHE = new HashMap<>();

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public LastModifiedCashingWire(final Wire wire) {
        this.origin = wire;
    }

    // @checkstyle ParameterNumber (5 lines)
    @Override
    public final Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read) throws IOException {
        final URI uri = req.uri().get();
        final StringBuilder queryBuilder = new StringBuilder(Tv.HUNDRED)
            .append(method).append(' ').append(uri.getPath());
        if (uri.getQuery() != null) {
            queryBuilder.append('?').append(uri.getQuery());
        }
        final String query = queryBuilder.toString();
        if (!method.equals(Request.GET)) {
            return this.origin.send(
                req, home, method, headers, content, connect, read
            );
        }
        return this.lookInCache(
            req, home, method, headers, content, connect, read, query
        );
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
     * @param query The key of the request
     * @return Response obtained
     * @throws IOException if fails
     * @checkstyle ParameterNumber (6 lines)
     */
    private Response lookInCache(final Request req,
        final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read,
        final String query) throws IOException {
        final Response rspReceived;
        if (CACHE.containsKey(query)) {
            return this.updateCache(
                req, home, method, headers, content, connect, read, query
            );
        } else {
            rspReceived = this.origin.send(
                req, home, method, headers, content, connect, read
            );
            this.addToCache(query, rspReceived);
            return rspReceived;
        }
    }

    /**
     * Check response and update cache if needed.
     *
     * @param req Request
     * @param home URI to fetch
     * @param method HTTP method
     * @param headers Headers
     * @param content HTTP body
     * @param connect The connect timeout
     * @param read The read timeout
     * @param query The key of the request
     * @return Response obtained
     * @throws IOException if fails
     * @checkstyle ParameterNumber (8 lines)
     */
    private Response updateCache(final Request req,
        final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read,
        final String query) throws IOException {
        final Response rspReceived;
        final Response rspCashed = CACHE.get(query);
        final Collection<Map.Entry<String, String>> hdrs = this.enrich(
            headers, rspCashed
        );
        rspReceived = this.origin.send(
            req, home, method, hdrs, content, connect, read
        );
        if (rspReceived.status() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            return rspCashed;
        } else {
            this.addToCache(query, rspReceived);
            return rspReceived;
        }
    }

    /**
     * Add response to cache.
     *
     * @param query The key of the request
     * @param rsp The response to add
     */
    private void addToCache(final String query, final Response rsp) {
        if (rsp.headers().containsKey(HEADER_LAST_MODIFIED)) {
            CACHE.put(query, rsp);
        }
    }

    /**
     * Add Last-Modified modified header.
     *
     * @param headers Original headers
     * @param rsp Cached response
     * @return Map with If-Modified-Since header
     */
    private Collection<Map.Entry<String, String>> enrich(
        final Collection<Map.Entry<String, String>> headers,
        final Response rsp) {
        final List<String> list = rsp.headers().get(HEADER_LAST_MODIFIED);
        final Map<String, String> map = new HashMap<>(headers.size() + 1);
        for (final Map.Entry<String, String> entry : headers) {
            map.put(entry.getKey(), entry.getValue());
        }
        map.put(HEADER_IF_MODIFIED_SINCE, list.iterator().next());
        return map.entrySet();
    }
}
