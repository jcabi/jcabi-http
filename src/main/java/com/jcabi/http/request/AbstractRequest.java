/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import com.jcabi.http.RequestURI;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import lombok.EqualsAndHashCode;

/**
 * Request that delegates all calls to an encapsulated request.
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @since 2.0
 */
@Immutable
@EqualsAndHashCode(of = "base")
@Loggable(Loggable.DEBUG)
abstract class AbstractRequest implements Request {

    /**
     * Base request.
     */
    private final transient Request base;

    /**
     * Ctor.
     *
     * @param req Request to delegate to
     */
    AbstractRequest(final Request req) {
        this.base = req;
    }

    @Override
    public final String toString() {
        return String.format(
            "%s(base=%s)", this.getClass().getSimpleName(), this.base
        );
    }

    @Override
    public final RequestURI uri() {
        return this.base.uri();
    }

    @Override
    public final Request header(final String name, final Object value) {
        return this.base.header(name, value);
    }

    @Override
    public final Request reset(final String name) {
        return this.base.reset(name);
    }

    @Override
    public final RequestBody body() {
        return this.base.body();
    }

    @Override
    public final RequestBody multipartBody() {
        return this.base.multipartBody();
    }

    @Override
    public final Request method(final String method) {
        return this.base.method(method);
    }

    @Override
    public final Request timeout(final int connect, final int read) {
        return this.base.timeout(connect, read);
    }

    @Override
    public final Response fetch() throws IOException {
        return this.base.fetch();
    }

    @Override
    public final Response fetch(final InputStream stream) throws IOException {
        return this.base.fetch(stream);
    }

    @Override
    public final <T extends Wire> Request through(
        final Class<T> type,
        final Object... args
    ) {
        return this.base.through(type, args);
    }

    @Override
    public final Request through(final Wire wire) {
        return this.base.through(wire);
    }
}
