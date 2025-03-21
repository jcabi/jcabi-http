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
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * Implementation of {@link Request},
 * based on Apache HTTP client.
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.8
 * @todo #200:30m TrustedWire does not support ApacheRequest.
 *  Investigate if it's possible for them to work together,
 *  if not see jcabi-http#178 for discussion about alternative solutions.
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@Immutable
@EqualsAndHashCode(of = "base")
@ToString(of = "base")
@Loggable(Loggable.DEBUG)
@SuppressWarnings("PMD.TooManyMethods")
public final class ApacheRequest implements Request {

    /**
     * The wire to use.
     * @checkstyle AnonInnerLength (200 lines)
     */
    private static final Wire WIRE = new Wire() {
        // @checkstyle ParameterNumber (6 lines)
        @Override
        public Response send(final Request req, final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final InputStream content,
            final int connect,
            final int read) throws IOException {
            try (CloseableHttpResponse response = HttpClients.createSystem().execute(
                this.httpRequest(
                    home, method, headers, content,
                    connect, read
                )
            )) {
                return new DefaultResponse(
                    req,
                    response.getStatusLine().getStatusCode(),
                    response.getStatusLine().getReasonPhrase(),
                    this.headers(response.getAllHeaders()),
                    this.consume(response.getEntity())
                );
            }
        }

        /**
         * Create request.
         * @param home Home URI
         * @param method Method to use
         * @param headers HTTP Headers to use
         * @param content Content to send
         * @param connect Connect timeout
         * @param read Read timeout
         * @return Request
         * @throws IOException If an IO Exception occurs
         * @checkstyle ParameterNumber (6 lines)
         */
        public HttpEntityEnclosingRequestBase httpRequest(final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final InputStream content,
            final int connect,
            final int read) throws IOException {
            final HttpEntityEnclosingRequestBase req =
                new HttpEntityEnclosingRequestBase() {
                    @Override
                    public String getMethod() {
                        return method;
                    }
                };
            final URI uri = URI.create(home);
            req.setConfig(
                RequestConfig.custom()
                    .setCircularRedirectsAllowed(false)
                    .setRedirectsEnabled(false)
                    .setConnectTimeout(connect)
                    .setSocketTimeout(read)
                    .build()
            );
            req.setURI(uri);
            req.setEntity(
                new BufferedHttpEntity(new InputStreamEntity(content))
            );
            for (final Map.Entry<String, String> header : headers) {
                req.addHeader(header.getKey(), header.getValue());
            }
            return req;
        }

        /**
         * Fetch body from http entity.
         * @param entity HTTP entity
         * @return Body in UTF-8
         * @throws IOException If fails
         */
        private byte[] consume(final HttpEntity entity) throws IOException {
            final byte[] body;
            if (entity == null) {
                body = new byte[0];
            } else {
                body = EntityUtils.toByteArray(entity);
            }
            return body;
        }

        /**
         * Make a list of all hdrs.
         * @param list Apache HTTP hdrs
         * @return Body in UTF-8
         */
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        private Array<Map.Entry<String, String>> headers(final Header... list) {
            final Collection<Map.Entry<String, String>> headers =
                new LinkedList<>();
            for (final Header header : list) {
                headers.add(
                    new ImmutableHeader(
                        header.getName(),
                        header.getValue()
                    )
                );
            }
            return new Array<Map.Entry<String, String>>(headers);
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
    public ApacheRequest(final URL url) {
        this(url.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public ApacheRequest(final URI uri) {
        this(uri.toString());
    }

    /**
     * Public ctor.
     * @param uri The resource to work with
     */
    public ApacheRequest(final String uri) {
        this.base = new BaseRequest(ApacheRequest.WIRE, uri);
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
    public <T extends Wire> Request through(final Class<T> type,
        final Object... args) {
        return this.base.through(type, args);
    }

    @Override
    public Request through(final Wire wire) {
        return this.base.through(wire);
    }
}
