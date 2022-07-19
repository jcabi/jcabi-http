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
import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Verbose wire.
 *
 * <p>This wire makes HTTP request and response details visible in
 * log (we're using SLF4J logging facility), for example:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(VerboseWire.class)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.10
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class VerboseWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public VerboseWire(final Wire wire) {
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
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final byte[] buffer = new byte[Tv.THOUSAND];
        for (int bytes = content.read(buffer); bytes != -1;
            bytes = content.read(buffer)) {
            output.write(buffer, 0, bytes);
        }
        output.flush();
        final Response response = this.origin.send(
            req, home, method, headers,
            new ByteArrayInputStream(output.toByteArray()),
                connect, read
        );
        final StringBuilder text = new StringBuilder(0);
        for (final Map.Entry<String, String> header : headers) {
            text.append(header.getKey())
                .append(": ")
                .append(header.getValue())
                .append('\n');
        }
        text.append('\n').append(
            new RequestBody.Printable(output.toByteArray()).toString()
        );
        Logger.info(
            this,
            "#send(%s %s):\nHTTP Request (%s):\n%s\nHTTP Response (%s):\n%s",
            method, home,
            req.getClass().getName(),
            VerboseWire.indent(text.toString()),
            response.getClass().getName(),
            VerboseWire.indent(response.toString())
        );
        return response;
    }

    /**
     * Indent provided text.
     * @param text Text to indent
     * @return Indented text
     */
    private static String indent(final String text) {
        return new StringBuilder("  ")
            .append(text.replaceAll("(\n|\n\r)", "$1  "))
            .toString();
    }

}
