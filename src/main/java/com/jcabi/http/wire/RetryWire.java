/**
 * Copyright (c) 2011-2015, jcabi.com
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
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.log.Logger;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Wire that retries a few times before giving up and throwing exception.
 *
 * <p>This wire retries again (at least three times) if an original one throws
 * {@link IOException}:
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(RetryWire.class)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>Since version 1.9 this wire retries also if HTTP status code
 * is between 500 and 599.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 */
@Immutable
@ToString
@EqualsAndHashCode(of = "origin")
public final class RetryWire implements Wire {

    /**
     * Pattern used to hide basic auth when logging URL
     *
     * eg. http://user:pass@localhost/context will product three groups
     *     1. http://
     *     2. user:pass@
     *     3. localhost:80/context
     */
    private static final Pattern GROUP_SCHEMA_BASIC_AUTH_HOST_CONTEXT_PATH = Pattern.compile("(.+://)(.+:.+@)(.*)");

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public RetryWire(@NotNull(message = "wire can't be NULL")
        final Wire wire) {
        this.origin = wire;
    }

    // @checkstyle ParameterNumber (13 lines)
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content) throws IOException {
        int attempt = 0;
        while (true) {
            if (attempt > Tv.THREE) {
                throw new IOException(
                    String.format("failed after %d attempts", attempt)
                );
            }
            try {
                final Response rsp = this.origin.send(
                    req, home, method, headers, content
                );
                if (rsp.status() < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    return rsp;
                }
                Logger.warn(
                    this, "%s %s returns %d status (attempt #%d)",
                    method, hideAnyBasicAuth(home), rsp.status(), attempt + 1
                );
            } catch (final IOException ex) {
                Logger.warn(
                    this, "%s: %s",
                    ex.getClass().getName(), ex.getLocalizedMessage()
                );
            }
            ++attempt;
        }
    }

    private String hideAnyBasicAuth(final String home) {
        final Matcher m = GROUP_SCHEMA_BASIC_AUTH_HOST_CONTEXT_PATH.matcher(home);

        if(m.find() && m.groupCount() == 3) {
            return String.format("%s****:****@%s", m.group(1), m.group(3));
        } else {
            return home;
        }
    }
}
