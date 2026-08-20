/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

/**
 * The SSL defaults of the JVM, as they were before we changed them.
 * @since 1.10
 */
@Immutable
final class Defaults {

    /**
     * Default socket factory.
     */
    private final SSLSocketFactory factory;

    /**
     * Default context.
     */
    private final SSLContext context;

    /**
     * Ctor.
     * @param sockets Default socket factory
     * @param ctx Default context
     */
    Defaults(final SSLSocketFactory sockets, final SSLContext ctx) {
        this.factory = sockets;
        this.context = ctx;
    }

    /**
     * Put them back into the JVM.
     */
    void restore() {
        HttpsURLConnection.setDefaultSSLSocketFactory(this.factory);
        SSLContext.setDefault(this.context);
    }
}
