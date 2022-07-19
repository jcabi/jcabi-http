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
import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.xml.bind.DatatypeConverter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Wire with HTTP basic authentication based on user info of URI.
 *
 * <p>This wire converts user info from URI into
 * {@code "Authorization"} HTTP header, for example:
 *
 * <pre> String html = new JdkRequest("http://jeff:12345@example.com")
 *   .through(BasicAuthWire.class)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>In this example, an additional HTTP header {@code Authorization}
 * will be added with a value {@code Basic amVmZjoxMjM0NQ==}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.10
 * @see <a href="http://tools.ietf.org/html/rfc2617">RFC 2617 "HTTP Authentication: Basic and Digest Access Authentication"</a>
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class BasicAuthWire implements Wire {

    /**
     * The Charset to use.
     */
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public BasicAuthWire(final Wire wire) {
        this.origin = wire;
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
            if (header.getKey().equals(HttpHeaders.AUTHORIZATION)) {
                Logger.warn(
                    this,
                    "Request already contains %s header",
                    HttpHeaders.AUTHORIZATION
                );
                absent = false;
            }
            hdrs.add(header);
        }
        final String info = URI.create(home).getUserInfo();
        if (absent && info != null) {
            final String[] parts = info.split(":", 2);
            hdrs.add(
                new ImmutableHeader(
                    HttpHeaders.AUTHORIZATION,
                    Logger.format(
                        "Basic %s",
                        DatatypeConverter.printBase64Binary(
                            Logger.format(
                                "%s:%s",
                                parts[0],
                                parts[1]
                            ).getBytes(BasicAuthWire.CHARSET)
                        )
                    )
                )
            );
        }
        return this.origin.send(
            req.uri().userInfo(null).back(),
            home,
            method,
            hdrs,
            content,
            connect,
            read
        );
    }
}
