/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import com.jcabi.http.ImmutableHeader;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Cookies received while following a chain of HTTP redirects.
 *
 * <p>The jar remembers cookies set by {@code Set-Cookie} headers of
 * responses and attaches the applicable ones to the next request,
 * following the domain, path, secure and expiration rules of
 * RFC 6265. Cookies are never sent to a host they don't belong to.</p>
 *
 * <p>The class is NOT thread-safe, a new instance is supposed to be
 * created for every chain of redirects.</p>
 *
 * @see <a href="https://tools.ietf.org/html/rfc6265">RFC 6265 "HTTP State Management Mechanism"</a>
 * @since 2.0
 */
final class CookieJar {

    /**
     * Separator of cookies in a {@code Cookie} header.
     */
    private static final Splitter SEMICOLON =
        Splitter.on(';').trimResults().omitEmptyStrings();

    /**
     * Cookies received so far.
     */
    private final transient List<HttpCookie> cookies;

    /**
     * Ctor.
     */
    CookieJar() {
        this.cookies = new ArrayList<>(0);
    }

    /**
     * Remember cookies set by a response.
     *
     * <p>Malformed cookies, cookies for domains the responding host
     * doesn't belong to and cookies for public suffixes, like
     * {@code com} or {@code co.uk}, are ignored. Expired cookies remove
     * previously received cookies with the same name, domain and
     * path.</p>
     *
     * @param uri URI the response was received from
     * @param headers Headers of the response
     */
    void store(final URI uri, final Map<String, List<String>> headers) {
        for (final Map.Entry<String, List<String>> header
            : headers.entrySet()) {
            if (HttpHeaders.SET_COOKIE.equalsIgnoreCase(header.getKey())) {
                for (final String value : header.getValue()) {
                    this.store(uri, value);
                }
            }
        }
    }

    /**
     * Attach applicable cookies to the headers of a request.
     *
     * <p>If there are no cookies for the URI, the headers are returned
     * as is. Otherwise all {@code Cookie} headers are merged into one,
     * where the received cookies replace the ones with the same
     * names. Of the received cookies with the same name, the one with
     * the longest path is sent.</p>
     *
     * @param uri URI the request is going to be sent to
     * @param headers Headers of the request
     * @return Headers with cookies
     */
    Collection<Map.Entry<String, String>> attach(final URI uri,
        final Collection<Map.Entry<String, String>> headers) {
        final Map<String, String> received = new LinkedHashMap<>(0);
        final List<HttpCookie> applicable = this.cookies.stream()
            .filter(cookie -> CookieJar.matches(cookie, uri))
            .sorted(Comparator.comparingInt(cookie -> cookie.getPath().length()))
            .collect(Collectors.toList());
        for (final HttpCookie cookie : applicable) {
            received.put(
                cookie.getName(),
                String.format("%s=%s", cookie.getName(), cookie.getValue())
            );
        }
        final Collection<Map.Entry<String, String>> result;
        if (received.isEmpty()) {
            result = headers;
        } else {
            result = new ArrayList<>(headers.size() + 1);
            final Map<String, String> merged = new LinkedHashMap<>(0);
            for (final Map.Entry<String, String> header : headers) {
                if (HttpHeaders.COOKIE.equalsIgnoreCase(header.getKey())) {
                    CookieJar.split(header.getValue(), merged);
                } else {
                    result.add(header);
                }
            }
            merged.putAll(received);
            result.add(
                new ImmutableHeader(
                    HttpHeaders.COOKIE, String.join("; ", merged.values())
                )
            );
        }
        return result;
    }

    private void store(final URI uri, final String header) {
        final String host = CookieJar.host(uri);
        for (final HttpCookie cookie : CookieJar.parse(header)) {
            final String domain = CookieJar.domain(cookie, host);
            if (!domain.isEmpty()) {
                cookie.setDomain(domain);
                cookie.setPath(CookieJar.path(cookie, uri));
                this.cookies.remove(cookie);
                if (!cookie.hasExpired()) {
                    this.cookies.add(cookie);
                }
            }
        }
    }

    private static List<HttpCookie> parse(final String header) {
        List<HttpCookie> parsed;
        try {
            parsed = HttpCookie.parse(header);
        } catch (final IllegalArgumentException ex) {
            parsed = Collections.emptyList();
        }
        return parsed;
    }

    private static String domain(final HttpCookie cookie, final String host) {
        final String suffix = CookieJar.withoutDot(
            Strings.nullToEmpty(cookie.getDomain()).toLowerCase(Locale.ENGLISH)
        );
        final String domain;
        if (suffix.isEmpty()
            || suffix.equals(host) && CookieJar.isPublic(suffix)) {
            domain = host;
        } else if (CookieJar.domainMatches(host, suffix)
            && !CookieJar.isPublic(suffix)) {
            domain = String.format(".%s", suffix);
        } else {
            domain = "";
        }
        return domain;
    }

    private static String path(final HttpCookie cookie, final URI uri) {
        final String attr = cookie.getPath();
        final String path;
        if (attr == null || attr.isEmpty() || attr.charAt(0) != '/') {
            final String dir = CookieJar.requested(uri);
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

    private static boolean matches(final HttpCookie cookie, final URI uri) {
        return CookieJar.owns(cookie, CookieJar.host(uri))
            && !cookie.hasExpired()
            && CookieJar.transportable(cookie, uri)
            && CookieJar.pathMatches(CookieJar.requested(uri), cookie.getPath());
    }

    private static boolean owns(final HttpCookie cookie, final String host) {
        final String domain = cookie.getDomain();
        final boolean owns;
        if (domain.charAt(0) == '.') {
            owns = CookieJar.domainMatches(host, domain.substring(1));
        } else {
            owns = host.equals(domain);
        }
        return owns;
    }

    private static boolean transportable(final HttpCookie cookie,
        final URI uri) {
        return !cookie.getSecure() || "https".equalsIgnoreCase(uri.getScheme());
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

    private static boolean pathMatches(final String path, final String dir) {
        return path.equals(dir)
            || path.startsWith(dir)
            && (dir.endsWith("/") || path.charAt(dir.length()) == '/');
    }

    private static String requested(final URI uri) {
        final String raw = uri.getRawPath();
        final String path;
        if (raw == null || raw.isEmpty() || raw.charAt(0) != '/') {
            path = "/";
        } else {
            path = raw;
        }
        return path;
    }

    private static String host(final URI uri) {
        final String host = uri.getHost();
        final String lower;
        if (host == null) {
            lower = "";
        } else {
            lower = host.toLowerCase(Locale.ENGLISH);
        }
        return lower;
    }

    private static String withoutDot(final String domain) {
        final String clean;
        if (!domain.isEmpty() && domain.charAt(0) == '.') {
            clean = domain.substring(1);
        } else {
            clean = domain;
        }
        return clean;
    }

    private static void split(final String header,
        final Map<String, String> target) {
        for (final String cookie : CookieJar.SEMICOLON.split(header)) {
            final int split = cookie.indexOf('=');
            if (split < 0) {
                target.put(cookie, cookie);
            } else {
                target.put(cookie.substring(0, split).trim(), cookie);
            }
        }
    }
}
