/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.RequestURI;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Base URI.
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode(of = "address")
@Loggable(Loggable.DEBUG)
final class BaseUri implements RequestURI {

    /**
     * URI encapsulated.
     */
    private final transient String address;

    /**
     * Base request encapsulated.
     */
    private final transient BaseRequest owner;

    /**
     * Public ctor.
     * @param req Request
     * @param uri The URI to start with
     */
    BaseUri(final BaseRequest req, final String uri) {
        this.owner = req;
        this.address = uri;
    }

    @Override
    public String toString() {
        return this.address;
    }

    @Override
    public Request back() {
        return this.owner.uri(this.address);
    }

    @Override
    public URI get() {
        return URI.create(this.owner.home());
    }

    @Override
    public RequestURI set(final URI uri) {
        return new BaseUri(this.owner, uri.toString());
    }

    @Override
    public RequestURI queryParam(final String name, final Object value) {
        return new BaseUri(
            this.owner,
            UriBuilder.fromUri(this.address)
                .queryParam(name, "{value}")
                .build(value).toString()
        );
    }

    @Override
    public RequestURI queryParams(final Map<String, String> map) {
        final UriBuilder uri = UriBuilder.fromUri(this.address);
        final Object[] values = new Object[map.size()];
        int idx = 0;
        for (final Map.Entry<String, String> pair : map.entrySet()) {
            uri.queryParam(pair.getKey(), String.format("{x%d}", idx));
            values[idx] = pair.getValue();
            ++idx;
        }
        return new BaseUri(
            this.owner,
            uri.build(values).toString()
        );
    }

    @Override
    public RequestURI path(final String segment) {
        return new BaseUri(
            this.owner,
            UriBuilder.fromUri(this.address)
                .path(segment)
                .build().toString()
        );
    }

    @Override
    public RequestURI userInfo(final String info) {
        return new BaseUri(
            this.owner,
            UriBuilder.fromUri(this.address)
                .userInfo(info)
                .build().toString()
        );
    }

    @Override
    public RequestURI port(final int num) {
        return new BaseUri(
            this.owner,
            UriBuilder.fromUri(this.address)
                .port(num).build().toString()
        );
    }
}
