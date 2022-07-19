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
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.HttpHeaders;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Auto Redirecting Wire.
 *
 * <p>This wire will retry a request a certain number of times (default: 5)
 * after a short delay when a HTTP response with a status code of 300-399 is
 * received. On every next attempt a new URL will be used, according
 * to the value of {@code Location} HTTP header of the response.
 *
 * <p>If the maximum number of retries are reached, the last response
 * received is returned to the caller, regardless of its status code.
 *
 * <pre> String html = new JdkRequest("http://goggle.com")
 *   .through(AutoRedirectingWire.class)
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN)
 *   .fetch()
 *   .body();</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 1.6
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = { "origin", "max" })
public final class AutoRedirectingWire implements Wire {
    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Maximum number of retries to be made.
     */
    private final transient int max;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public AutoRedirectingWire(final Wire wire) {
        this(wire, Tv.FIVE);
    }

    /**
     * Public ctor.
     * @param wire Original wire
     * @param retries Maximum number of retries
     */
    public AutoRedirectingWire(final Wire wire, final int retries) {
        this.origin = wire;
        this.max = retries;
    }

    // @checkstyle ParameterNumber (5 lines)
    @Override
    public Response send(final Request req, final String home,
        final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream content,
        final int connect,
        final int read) throws IOException {
        Response response = this.origin.send(
            req, home, method, headers, content, connect, read
        );
        int attempt = 1;
        final URI uri = URI.create(home);
        while (attempt < this.max) {
            if (response.status() < HttpURLConnection.HTTP_MULT_CHOICE
                || response.status() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                break;
            }
            final List<String> locations = response.headers().get(
                HttpHeaders.LOCATION
            );
            if (locations == null || locations.size() != 1) {
                break;
            }
            URI location = URI.create(locations.get(0));
            if (!location.isAbsolute()) {
                location = uri.resolve(location);
            }
            response = this.origin.send(
                req, location.toString(),
                method, headers, content, connect, read
            );
            try {
                TimeUnit.SECONDS.sleep((long) attempt);
            } catch (final InterruptedException ex) {
                throw new IOException(ex);
            }
            ++attempt;
        }
        return response;
    }

}
