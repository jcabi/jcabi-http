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
import java.util.List;
import java.util.Map;

/**
 * RESTful response returned by {@link Request#fetch()}.
 *
 * <p>You can get this response from one of implementations of {@link Request}:
 *
 * <pre> Response response = new JdkRequest("https://www.google.com")
 *   .header("Accept", "text/html")
 *   .fetch();</pre>
 *
 * <p>Instances of this interface are immutable and thread-safe.
 *
 * @since 0.8
 * @see com.jcabi.http.request.JdkRequest
 */
@Immutable
public interface Response {

    /**
     * Get back to the request it's related to.
     * @return The request we're in
     */
    Request back();

    /**
     * Get status of the response as a positive integer number.
     * @return The status code
     */
    int status();

    /**
     * Get status line reason phrase.
     * @return The status line reason phrase
     */
    String reason();

    /**
     * Get a collection of all headers.
     * @return The headers
     */
    Map<String, List<String>> headers();

    /**
     * Get body as a string, assuming it's {@code UTF-8} (if there is something
     * else that can't be translated into a UTF-8 string a runtime exception
     * will be thrown).
     *
     * <p><strong>DISCLAIMER</strong>:
     * The only encoding supported here is UTF-8. If the body of response
     * contains any chars that can't be used and should be replaced with
     * a "replacement character", a {@link RuntimeException} will be thrown. If
     * you need to use some other encodings, use
     * {@link #binary()} instead.
     *
     * @return The body, as a UTF-8 string
     */
    String body();

    /**
     * Raw body as a an array of bytes.
     * @return The body, as a UTF-8 string
     */
    byte[] binary();

    /**
     * Convert it to another type, by encapsulation.
     * @param type Type to use
     * @param <T> Type to use
     * @return New response
     */
    @SuppressWarnings("PMD.ShortMethodName")
    //@checkstyle MethodName (1 lines)
    <T extends Response> T as(Class<T> type);

}
