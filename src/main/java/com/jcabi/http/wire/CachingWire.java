/**
 * Copyright (c) 2011-2014, jcabi.com
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
package com.jcabi.http.wire;

import com.jcabi.aspects.Cacheable;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire that caches GET requests (for five minutes).
 *
 * <p>This decorator can be used when you want to avoid duplicate
 * GET requests to load-sensitive resources, for example:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(CachingWire.class)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 1.0
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class CachingWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public CachingWire(@NotNull(message = "wire can't be NULL")
        final Wire wire) {
        this.origin = wire;
    }

    /**
     * {@inheritDoc}
     * @checkstyle ParameterNumber (13 lines)
     */
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final byte[] content) throws IOException {
        final Response rsp;
        if (method.equals(Request.GET)) {
            rsp = this.get(req, home, headers, content);
        } else {
            rsp = this.origin.send(req, home, method, headers, content);
        }
        return rsp;
    }

    /**
     * Fetch GET request and cache it.
     * @param req Request
     * @param home URI to fetch
     * @param headers Headers
     * @param content HTTP body
     * @return Response obtained
     * @throws IOException if fails
     * @checkstyle ParameterNumber (13 lines)
     */
    @Cacheable(lifetime = Tv.FIVE, unit = TimeUnit.MINUTES)
    public Response get(final Request req, final String home,
        final Collection<Map.Entry<String, String>> headers,
        final byte[] content) throws IOException {
        return this.origin.send(req, home, Request.GET, headers, content);
    }
}
