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
