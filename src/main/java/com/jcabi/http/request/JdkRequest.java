/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
 * @since 0.8
 * // @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "base")
@ToString(of = "base")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class JdkRequest implements Request {

    /**
     * The wire to use.
     * @checkstyle AnonInnerLength (200 lines)
     */
    private static final Wire WIRE = new Wire() {
        // @checkstyle ParameterNumber (6 lines)
        @Override
        public Response send(
            final Request req, final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final InputStream content,
            final int connect,
            final int read
        ) throws IOException {
            final HttpURLConnection conn = JdkRequest.openConnection(home);
            try {
                conn.setConnectTimeout(connect);
                conn.setReadTimeout(read);
                conn.setRequestMethod(method);
                conn.setUseCaches(false);
                conn.setInstanceFollowRedirects(false);
                for (final Map.Entry<String, String> header : headers) {
                    conn.addRequestProperty(header.getKey(), header.getValue());
                }
                if (method.equals(Request.POST) || method.equals(Request.PUT)
                    || method.equals(Request.PATCH)) {
                    conn.setDoOutput(true);
                    try (OutputStream output = conn.getOutputStream()) {
                        this.writeFully(content, output);
                    }
                }
                return new DefaultResponse(
                    req,
                    conn.getResponseCode(),
                    conn.getResponseMessage(),
                    this.headers(conn.getHeaderFields()),
                    this.body(conn)
                );
            } catch (final IOException exp) {
                throw new IOException(
                    String.format("Failed %s request to %s", method, home),
                    exp
                );
            } finally {
                conn.disconnect();
            }
        }

        /**
         * Fully write the input stream contents to the output stream.
         * @param content The content to write
         * @param output The output stream to write to
         * @throws IOException If an IO Exception occurs
         */
        private void writeFully(
            final InputStream content,
            final OutputStream output
        ) throws IOException {
            // @checkstyle MagicNumber (1 line)
            final byte[] buffer = new byte[8192];
            for (int bytes = content.read(buffer); bytes != -1;
                bytes = content.read(buffer)) {
                output.write(buffer, 0, bytes);
            }
        }

        /**
         * Get headers from response.
         * @param fields ImmutableHeader fields
         * @return Headers
         */
        private Array<Map.Entry<String, String>> headers(
            final Map<String, List<String>> fields
        ) {
            final Collection<Map.Entry<String, String>> headers =
                new LinkedList<>();
            for (final Map.Entry<String, List<String>> field
                : fields.entrySet()) {
                if (field.getKey() == null) {
                    continue;
                }
                for (final String value : field.getValue()) {
                    headers.add(new ImmutableHeader(field.getKey(), value));
                }
            }
            return new Array<>(headers);
        }

        /**
         * Get response body of connection.
         * @param conn Connection
         * @return Body
         * @throws IOException
         */
        private byte[] body(final HttpURLConnection conn) throws IOException {
            final InputStream inp;
            if (conn.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                inp = conn.getErrorStream();
            } else {
                inp = conn.getInputStream();
            }
            byte[] body = new byte[0];
            if (inp != null) {
                try (InputStream is = inp; ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                    final byte[] buffer = new byte[8192];
                    int bytes;
                    while (true) {
                        bytes = is.read(buffer);
                        if (bytes == -1) {
                            break;
                        }
                        os.write(buffer, 0, bytes);
                    }
                    body = os.toByteArray();
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
    public JdkRequest(final URL url) {
        this(url.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public JdkRequest(final URI uri) {
        this(uri.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public JdkRequest(final String uri) {
        this.base = new BaseRequest(JdkRequest.WIRE, uri);
    }

    @Override
    public RequestURI uri() {
        return this.base.uri();
    }

    @Override
    public Request header(final String name, final Object value) {
        return this.base.header(name, value);
    }

    @Override
    public Request reset(final String name) {
        return this.base.reset(name);
    }

    @Override
    public RequestBody body() {
        return this.base.body();
    }

    @Override
    public RequestBody multipartBody() {
        return this.base.multipartBody();
    }

    @Override
    public Request method(final String method) {
        return this.base.method(method);
    }

    @Override
    public Request timeout(final int connect, final int read) {
        return this.base.timeout(connect, read);
    }

    @Override
    public Response fetch() throws IOException {
        return this.base.fetch();
    }

    @Override
    public Response fetch(final InputStream stream) throws IOException {
        return this.base.fetch(stream);
    }

    @Override
    public <T extends Wire> Request through(
        final Class<T> type,
        final Object... args
    ) {
        return this.base.through(type, args);
    }

    @Override
    public Request through(final Wire wire) {
        return this.base.through(wire);
    }

    /**
     * Open HTTP connection.
     * @param url URL.
     * @return Connection.
     * @throws IOException if unable to connect.
     */
    private static HttpURLConnection openConnection(
        final String url
    ) throws IOException {
        final URLConnection raw;
        try {
            raw = new URI(url).toURL().openConnection();
        } catch (final URISyntaxException | IllegalArgumentException ex) {
            throw new IOException(
                String.format("'%s' is incorrect", url),
                ex
            );
        }
        if (!(raw instanceof HttpURLConnection)) {
            throw new IOException(
                String.format(
                    "'%s' opens %s instead of expected HttpURLConnection",
                    url, raw.getClass().getName()
                )
            );
        }
        return HttpURLConnection.class.cast(raw);
    }

}
