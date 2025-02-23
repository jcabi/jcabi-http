/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Abstract response.
 *
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(of = "response")
abstract class AbstractResponse implements Response {

    /**
     * Encapsulated response.
     */
    private final transient Response response;

    /**
     * Ctor.
     * @param resp Response
     */
    AbstractResponse(final Response resp) {
        this.response = resp;
    }

    @Override
    public final String toString() {
        return this.response.toString();
    }

    @Override
    public final Request back() {
        return this.response.back();
    }

    @Override
    public final int status() {
        return this.response.status();
    }

    @Override
    public final String reason() {
        return this.response.reason();
    }

    @Override
    public final Map<String, List<String>> headers() {
        return this.response.headers();
    }

    @Override
    public String body() {
        return this.response.body();
    }

    @Override
    public final byte[] binary() {
        return this.response.binary();
    }

    // @checkstyle MethodName (4 lines)
    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    public final <T extends Response> T as(final Class<T> type) {
        return this.response.as(type);
    }

}
