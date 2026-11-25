/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Locale;

/**
 * Domain and path rules of RFC 6265, applied to cookies at a URI.
 *
 * <p>The class is immutable and thread-safe.</p>
 *
 * @since 2.0
 */
final class CookieScope {

    /**
     * URI of a request.
     */
    private final transient URI uri;

    /**
     * Ctor.
     *
     * @param address URI of a request
     */
    CookieScope(final URI address) {
        this.uri = address;
    }

    /**
     * Domain to keep a cookie under, when it is set by a response from
     * the URI.
     *
     * <p>The domain starts with a dot, unless the cookie belongs to the
     * responding host only.</p>
     *
     * @param cookie Cookie received
     * @return The domain or an empty string if the cookie must be ignored
     */
    String domain(final HttpCookie cookie) {
        final String host = this.host();
        final String suffix = CookieScope.withoutDot(
            Strings.nullToEmpty(cookie.getDomain()).toLowerCase(Locale.ENGLISH)
        );
        final String domain;
        if (suffix.isEmpty()
            || suffix.equals(host) && CookieScope.isPublic(suffix)) {
            domain = host;
        } else if (CookieScope.domainMatches(host, suffix)
            && !CookieScope.isPublic(suffix)) {
            domain = String.format(".%s", suffix);
        } else {
            domain = "";
        }
        return domain;
    }

    /**
     * Path to keep a cookie under, when it is set by a response from
     * the URI.
     *
     * @param cookie Cookie received
     * @return Path of the cookie or the default path of the URI
     */
    String path(final HttpCookie cookie) {
        final String attr = cookie.getPath();
        final String path;
        if (attr == null || attr.isEmpty() || attr.charAt(0) != '/') {
            final String dir = this.requested();
            final int slash = dir.lastIndexOf('/');
            if (slash <= 0) {
                path = "/";
            } else {
                path = dir.substring(0, slash);
            }
        } else {
            path = attr;
        }
        return path;
    }

    /**
     * Should a cookie be sent to the URI?
     *
     * @param cookie Cookie with the domain and the path already set
     * @return TRUE if it is applicable and not expired
     */
    boolean accepts(final HttpCookie cookie) {
        return this.owns(cookie)
            && !cookie.hasExpired()
            && this.transportable(cookie)
            && this.covers(cookie.getPath());
    }

    /**
     * Remove the leading dot of a domain, if it has one.
     *
     * @param domain The domain
     * @return The domain without the dot
     */
    static String withoutDot(final String domain) {
        final String clean;
        if (!domain.isEmpty() && domain.charAt(0) == '.') {
            clean = domain.substring(1);
        } else {
            clean = domain;
        }
        return clean;
    }

    private boolean owns(final HttpCookie cookie) {
        final String domain = cookie.getDomain();
        final boolean owns;
        if (domain.charAt(0) == '.') {
            owns = CookieScope.domainMatches(this.host(), domain.substring(1));
        } else {
            owns = this.host().equals(domain);
        }
        return owns;
    }

    private boolean transportable(final HttpCookie cookie) {
        return !cookie.getSecure()
            || "https".equalsIgnoreCase(this.uri.getScheme());
    }

    private boolean covers(final String dir) {
        final String path = this.requested();
        return path.equals(dir)
            || path.startsWith(dir)
            && (dir.endsWith("/") || path.charAt(dir.length()) == '/');
    }

    private String requested() {
        final String raw = this.uri.getRawPath();
        final String path;
        if (raw == null || raw.isEmpty() || raw.charAt(0) != '/') {
            path = "/";
        } else {
            path = raw;
        }
        return path;
    }

    private String host() {
        return Strings.nullToEmpty(this.uri.getHost())
            .toLowerCase(Locale.ENGLISH);
    }

    private static boolean domainMatches(final String host,
        final String domain) {
        return host.equals(domain)
            || host.endsWith(String.format(".%s", domain))
            && !InetAddresses.isUriInetAddress(host);
    }

    private static boolean isPublic(final String domain) {
        return InternetDomainName.isValid(domain)
            && InternetDomainName.from(domain).isPublicSuffix();
    }
}
