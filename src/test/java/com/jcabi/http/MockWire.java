/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map.Entry;

/**
 * Utility wire used for injecting a mock object into a {@link Request}
 * implementation.
 * <p>
 * NOTE: This is not threadsafe and access to it should be synchronized.
 *
 * @since 1.17.1
 * @checkstyle ParameterNumberCheck (50 lines)
 */
public class MockWire implements Wire {

    /**
     * The actual mock object we delegate the <code>Wire.send</code> call to.
     */
    private static Wire mockDelegate;

    /**
     * Creates a new mock wire instance.
     * <p>
     * The given target wire is ignored and <code>Wire.send</code> is delegated
     * to the static mock delegate.
     *
     * @param wire The original wire which is ignored
     */
    @SuppressWarnings("PMD.UnusedFormalParameter")
    public MockWire(final Wire wire) {
        // Instantiated by a Request implementation, wire is ignored
    }

    @Override
    public final Response send(final Request req, final String home,
        final String method, final Collection<Entry<String, String>> headers,
        final InputStream content, final int connect, final int read)
        throws IOException {
        return mockDelegate.send(
            req,
            home,
            method,
            headers,
            content,
            connect,
            read
        );
    }

    /**
     * Sets the mock the <code>Request.send</code> method is delegated to.
     *
     * @param mock The mock to assert variables passed by the request
     *  implementation
     */
    @SuppressWarnings("PMD.DefaultPackage")
    static void setMockDelegate(final Wire mock) {
        MockWire.mockDelegate = mock;
    }

}
