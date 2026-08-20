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
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Query.
 * @since 1.8.3
 */
@ToString
@EqualsAndHashCode(of = {"origin", "request", "uri", "headers"})
final class Query implements Callable<Response> {

    /**
     * Origin wire.
     */
    private final transient Wire origin;

    /**
     * Request.
     */
    private final transient Request request;

    /**
     * URI.
     */
    private final transient String uri;

    /**
     * Headers.
     */
    private final transient Collection<Map.Entry<String, String>> headers;

    /**
     * Body.
     */
    private final transient InputStream body;

    /**
     * Connect timeout.
     */
    private final transient int connect;

    /**
     * Read timeout.
     */
    private final transient int read;

    /**
     * Ctor.
     * @param wire Original wire
     * @param req Request
     * @param home URI to fetch
     * @param hdrs Headers
     * @param input Input body
     * @param cnct Connect timeout
     * @param rdd Read timeout
     */
    Query(
        final Wire wire, final Request req, final String home,
        final Collection<Map.Entry<String, String>> hdrs,
        final InputStream input, final int cnct,
        final int rdd
    ) {
        this.origin = wire;
        this.request = req;
        this.uri = home;
        this.headers = hdrs;
        this.body = input;
        this.connect = cnct;
        this.read = rdd;
    }

    @Override
    public Response call() throws IOException {
        return this.origin.send(
            this.request, this.uri, Request.GET, this.headers, this.body,
            this.connect, this.read
        );
    }
}
