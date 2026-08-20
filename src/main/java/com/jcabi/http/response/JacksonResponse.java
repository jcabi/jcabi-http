/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import lombok.EqualsAndHashCode;

/**
 * A JSON response provided by the Jackson Project.
 * @since 1.17
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class JacksonResponse extends AbstractResponse {

    /**
     * Ctor.
     * @param resp Response
     */
    public JacksonResponse(final Response resp) {
        super(resp);
    }

    /**
     * Read the body as JSON.
     * @return JSON reader
     */
    public JsonReader json() {
        return new JsonReader(this.binary());
    }
}
