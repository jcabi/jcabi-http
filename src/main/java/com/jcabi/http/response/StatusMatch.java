/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.Response;
import org.hamcrest.CustomMatcher;

/**
 * Status matcher.
 * @since 1.2
 */
final class StatusMatch extends CustomMatcher<Response> {

    /**
     * HTTP status to check.
     */
    private final transient int status;

    /**
     * Ctor.
     * @param msg Message to show
     * @param sts HTTP status to check
     */
    StatusMatch(final String msg, final int sts) {
        super(msg);
        this.status = sts;
    }

    @Override
    public boolean matches(final Object resp) {
        return Response.class.cast(resp).status() == this.status;
    }
}
