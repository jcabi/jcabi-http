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
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import com.jcabi.http.RequestURI;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.immutable.Array;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Implementation of {@link Request}, based on JDK.
 *
 * <p>This implementation will be enough in most situations. However,
 * sometimes you may need better tuning or an ability to fetch custom
 * HTTP methods (JDK doesn't support PATCH, for example). In this case,
 * use {@link ApacheRequest}.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "base")
@Loggable(Loggable.DEBUG)
public final class JdkRequest implements Request {

    /**
     * The wire to use.
     * @checkstyle AnonInnerLength (200 lines)
     */
    private static final Wire WIRE = new Wire() {
        /**
         * {@inheritDoc}
         * @checkstyle ParameterNumber (6 lines)
         */
        @Override
        public Response send(final Request req, final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final byte[] content) throws IOException {
            final URLConnection raw = new URL(home).openConnection();
            if (!(raw instanceof HttpURLConnection)) {
                throw new IOException(
                    String.format(
                        "'%s' opens %s instead of expected HttpURLConnection",
                        home, raw.getClass().getName()
                    )
                );
            }
            final HttpURLConnection conn = HttpURLConnection.class.cast(raw);
            try {
                conn.setRequestMethod(method);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);
                for (final Map.Entry<String, String> header : headers) {
                    conn.addRequestProperty(header.getKey(), header.getValue());
                }
                if (method.equals(Request.POST) || method.equals(Request.PUT)
                    || method.equals(Request.PATCH)) {
                    conn.setDoOutput(true);
                    final OutputStream output = conn.getOutputStream();
                    try {
                        output.write(content);
                    } finally {
                        output.close();
                    }
                }
                return new DefaultResponse(
                    req,
                    conn.getResponseCode(),
                    conn.getResponseMessage(),
                    this.headers(conn.getHeaderFields()),
                    this.body(conn)
                );
            } finally {
                conn.disconnect();
            }
        }
        /**
         * Get headers from response.
         * @param fields ImmutableHeader fields
         * @return Headers
         */
        private Array<Map.Entry<String, String>> headers(
            final Map<String, List<String>> fields) {
            final Collection<Map.Entry<String, String>> headers =
                new LinkedList<Map.Entry<String, String>>();
            for (final Map.Entry<String, List<String>> field
                : fields.entrySet()) {
                if (field.getKey() == null) {
                    continue;
                }
                for (final String value : field.getValue()) {
                    headers.add(new ImmutableHeader(field.getKey(), value));
                }
            }
            return new Array<Map.Entry<String, String>>(headers);
        }
        /**
         * Get response body of connection.
         * @param conn Connection
         * @return Body
         * @throws IOException
         */
        private byte[] body(final HttpURLConnection conn) throws IOException {
            final InputStream input;
            if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                input = conn.getErrorStream();
            } else {
                input = conn.getInputStream();
            }
            final byte[] body;
            if (input == null) {
                body = new byte[0];
            } else {
                try {
                    // @checkstyle MagicNumber (1 line)
                    final byte[] buffer = new byte[8192];
                    final ByteArrayOutputStream output =
                        new ByteArrayOutputStream();
                    for (int bytes = input.read(buffer); bytes != -1;
                        bytes = input.read(buffer)) {
                        output.write(buffer, 0, bytes);
                    }
                    body = output.toByteArray();
                } finally {
                    input.close();
                }
            }
            return body;
        }
    };

    /**
     * Base request.
     */
    private final transient Request base;

    /**
     * Public ctor.
     * @param url The resource to work with
     */
    public JdkRequest(@NotNull(message = "URL can't be NULL")
        final URL url) {
        this(url.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public JdkRequest(@NotNull(message = "URI can't be NULL")
        final URI uri) {
        this(uri.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public JdkRequest(@NotNull(message = "URI can't be NULL")
        final String uri) {
        this.base = new BaseRequest(JdkRequest.WIRE, uri);
    }

    @Override
    @NotNull
    public RequestURI uri() {
        return this.base.uri();
    }

    @Override
    public Request header(
        @NotNull(message = "header name can't be NULL") final String name,
        @NotNull(message = "header value can't be NULL") final Object value) {
        return this.base.header(name, value);
    }

    @Override
    public Request reset(
        @NotNull(message = "header name can't be NULL") final String name) {
        return this.base.reset(name);
    }

    @Override
    public RequestBody body() {
        return this.base.body();
    }

    @Override
    public Request method(
        @NotNull(message = "method can't be NULL") final String method) {
        return this.base.method(method);
    }

    @Override
    public Response fetch() throws IOException {
        return this.base.fetch();
    }

    @Override
    public <T extends Wire> Request through(final Class<T> type,
        final Object... args) {
        return this.base.through(type, args);
    }

}
