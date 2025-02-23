/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.log.Logger;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
 * @since 0.10
 */
@Immutable
@ToString(of = "origin")
@EqualsAndHashCode(of = "origin")
public final class RetryWire implements Wire {

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public ctor.
     * @param wire Original wire
     */
    public RetryWire(final Wire wire) {
        this.origin = wire;
    }

    // @checkstyle ParameterNumber (13 lines)
    @Override
    public Response send(final Request req, final String home, final String method,
        final Collection<Map.Entry<String, String>> hdrs, final InputStream cont,
        final int conn, final int read) throws IOException {
        int attempt = 0;
        while (true) {
            if (attempt > 3) {
                throw new IOException(
                    String.format("failed after %d attempts", attempt)
                );
            }
            try {
                final Response rsp = this.sendRequest(req, home, method, hdrs, cont, conn, read);
                if (rsp.status() < HttpURLConnection.HTTP_INTERNAL_ERROR) {
                    return rsp;
                }
                this.logWarning(method, home, rsp.status(), attempt);
            } catch (final IOException ex) {
                this.logWarning(ex);
            }
            ++attempt;
        }
    }

    // @checkstyle ParameterNumber (4 lines)
    private Response sendRequest(final Request req, final String home, final String method,
        final Collection<Map.Entry<String, String>> headers, final InputStream content,
        final int connect, final int read) throws IOException {
        return this.origin.send(req, home, method, headers, content, connect, read);
    }

    // @checkstyle ParameterNumber (17 lines)
    private void logWarning(final String method, final String home, final int status,
        final int attempt) {
        if (Logger.isWarnEnabled(this)) {
            final URI uri = URI.create(home);
            final String noauth = UriBuilder.fromUri(uri).userInfo("").toString();
            String authinfo = "";
            if (uri.getUserInfo() != null) {
                authinfo = Logger.format(
                    " (auth: %[secret]s)",
                    uri.getUserInfo()
                );
            }
            Logger.warn(
                this, "%s %s%s returns %d status (attempt #%d)",
                method, noauth, authinfo, status, attempt + 1
            );
        }
    }

    private void logWarning(final IOException exp) {
        if (Logger.isWarnEnabled(this)) {
            Logger.warn(
                this, "%s: %s",
                exp.getClass().getName(), exp.getLocalizedMessage()
            );
        }
    }
}
