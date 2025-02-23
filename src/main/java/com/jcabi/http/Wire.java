/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.aspects.Immutable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

/**
 * Wire.
 *
 * <p>An instance of this interface can be used in
 * {@link Request#through(Class,Object...)} to decorate
 * an existing {@code wire}, for example:
 *
 * <pre> String html = new JdkRequest("http://google.com")
 *   .through(VerboseWire.class)
 *   .through(RetryWire.class)
 *   .header("Accept", "text/html")
 *   .fetch()
 *   .body();</pre>
 *
 * <p>Every {@code Wire} decorator passed to {@code through()} method
 * wraps a previously existing one.
 *
 * @since 0.9
 */
@Immutable
//@checkstyle ParameterNumber (16 lines)
public interface Wire {

    /**
     * Send request and return response.
     * @param req Request
     * @param home URI to fetch
     * @param method HTTP method
     * @param headers Headers
     * @param content HTTP body
     * @param connect The connect timeout
     * @param read The read timeout
     * @return Response obtained
     * @throws IOException if fails
     */
    Response send(Request req, String home, String method,
        Collection<Map.Entry<String, String>> headers, InputStream content,
        int connect, int read)
        throws IOException;

}
