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
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.manifests.Manifests;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Wire with default user agent.
 *
 * <p>This wire adds an extra HTTP header {@code User-Agent} to the request,
 * if it's not yet provided, for example:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(UserAgentWire.class)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>An actual HTTP request will be sent with {@code User-Agent}
 * header with a value {@code ReXSL-0.1/abcdef0 Java/1.6} (for example). It
 * is recommended to use this wire decorator when you're working with
 * third party RESTful services, to properly identify yourself and avoid
 * troubles.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.10
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-14.43">RFC 2616 section 14.43 "User-Agent"</a>
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
@RequiredArgsConstructor
public final class UserAgentWire implements Wire {

    /**
     * Original wire.
     */
    private final Wire origin;

    /**
     * Agent.
     */
    private final String agent;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public UserAgentWire(final Wire wire) {
        this(
            wire,
            String.format(
                "jcabi-%s/%s Java/%s",
                Manifests.read("JCabi-Version"),
                Manifests.read("JCabi-Build"),
                System.getProperty("java.version")
            )
        );
    }

    // @checkstyle ParameterNumber (7 lines)
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read) throws IOException {
        final Collection<Map.Entry<String, String>> hdrs =
            new LinkedList<>();
        boolean absent = true;
        for (final Map.Entry<String, String> header : headers) {
            hdrs.add(header);
            if (header.getKey().equals(HttpHeaders.USER_AGENT)) {
                absent = false;
            }
        }
        if (absent) {
            hdrs.add(
                new ImmutableHeader(
                    HttpHeaders.USER_AGENT,
                    this.agent
                )
            );
        }
        return this.origin.send(
            req, home, method, hdrs, content, connect, read
        );
    }
}
