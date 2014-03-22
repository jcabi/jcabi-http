/**
 * Copyright (c) 2011-2014, JCabi.com
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
import java.net.URI;
import java.util.Map;
import javax.validation.constraints.NotNull;

/**
 * Request URI.
 *
 * <p>Instance of this interface is returned by {@link Request#uri()},
 * and can be modified using one of the methods below. When modification
 * is done, method {@code back()} returns a modified instance of
 * {@link Request}, for example:
 *
 * <pre> new JdkRequest("http://my.example.com")
 *   .header("Accept", "application/json")
 *   .uri()
 *   .path("/users")
 *   .queryParam("name", "Jeff Lebowski")
 *   .back() // returns a modified instance of Request
 *   .fetch()</pre>
 *
 * <p>Instances of this interface are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
public interface RequestURI {

    /**
     * Get back to the request it's related to.
     * @return The request we're in
     */
    @NotNull(message = "request is never NULL")
    Request back();

    /**
     * Get URI.
     * @return The destination it is currently pointing to
     */
    @NotNull(message = "URI is never NULL")
    URI get();

    /**
     * Set URI.
     * @param uri URI to set
     * @return New alternated URI
     */
    @NotNull(message = "URI is never NULL")
    RequestURI set(@NotNull(message = "URI can't be NULL") URI uri);

    /**
     * Add query param.
     * @param name Query param name
     * @param value Value of the query param to set
     * @return New alternated URI
     */
    @NotNull(message = "request URI is never NULL")
    RequestURI queryParam(
        @NotNull(message = "query param name can't be NULL") String name,
        @NotNull(message = "query param value can't be NULL") Object value);

    /**
     * Add query params.
     * @param map Map of params to add
     * @return New alternated URI
     */
    @NotNull(message = "request URI is never NULL")
    RequestURI queryParams(@NotNull(message = "map of params can't be NULL")
        Map<String, String> map);

    /**
     * Add URI path.
     * @param segment Path segment to add
     * @return New alternated URI
     */
    @NotNull(message = "request URI is never NULL")
    RequestURI path(@NotNull(message = "path can't be NULL") String segment);

    /**
     * Set user info.
     * @param info User info part to set
     * @return New alternated URI
     */
    @NotNull(message = "new request URI is never NULL")
    RequestURI userInfo(@NotNull(message = "info can't be NULL") String info);

    /**
     * Set port number.
     * @param num The port number to set
     * @return New altered URI
     */
    @NotNull(message = "new request URI is never NULL")
    RequestURI port(int num);

}
