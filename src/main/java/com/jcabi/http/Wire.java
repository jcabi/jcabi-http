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
