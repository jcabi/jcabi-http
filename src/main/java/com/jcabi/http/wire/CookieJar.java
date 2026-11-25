/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.google.common.base.Splitter;
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
import java.util.Map;
import java.util.regex.Pattern;
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
     * Valid value of the Max-Age attribute.
     */
    private static final Pattern MAX_AGE = Pattern.compile("-?\\d+");

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
     * names. All applicable received cookies are sent, the ones with
     * longer paths first.</p>
     *
     * @param uri URI the request is going to be sent to
     * @param headers Headers of the request
     * @return Headers with cookies
     */
    Collection<Map.Entry<String, String>> attach(final URI uri,
        final Collection<Map.Entry<String, String>> headers) {
        final List<HttpCookie> applicable = this.cookies.stream()
            .filter(new CookieScope(uri)::accepts)
            .sorted(Comparator.comparingInt(cookie -> -cookie.getPath().length()))
            .collect(Collectors.toList());
        final Collection<Map.Entry<String, String>> result;
        if (applicable.isEmpty()) {
            result = headers;
        } else {
            result = CookieJar.merge(headers, applicable);
        }
        return result;
    }

    private void store(final URI uri, final String header) {
        final CookieScope scope = new CookieScope(uri);
        for (final HttpCookie cookie : CookieJar.parse(header)) {
            final String domain = scope.domain(cookie);
            if (!domain.isEmpty()) {
                cookie.setDomain(domain);
                cookie.setPath(scope.path(cookie));
                this.cookies.removeIf(
                    stored -> CookieJar.same(stored, cookie)
                );
                if (!cookie.hasExpired() && !CookieJar.expires(header)) {
                    this.cookies.add(cookie);
                }
            }
        }
    }

    private static Collection<Map.Entry<String, String>> merge(
        final Collection<Map.Entry<String, String>> headers,
        final List<HttpCookie> received) {
        final Collection<Map.Entry<String, String>> result =
            new ArrayList<>(headers.size() + 1);
        final Map<String, String> own = new LinkedHashMap<>(0);
        for (final Map.Entry<String, String> header : headers) {
            if (HttpHeaders.COOKIE.equalsIgnoreCase(header.getKey())) {
                CookieJar.split(header.getValue(), own);
            } else {
                result.add(header);
            }
        }
        for (final HttpCookie cookie : received) {
            own.remove(cookie.getName());
        }
        final List<String> values = new ArrayList<>(own.values());
        for (final HttpCookie cookie : received) {
            values.add(
                String.format("%s=%s", cookie.getName(), cookie.getValue())
            );
        }
        result.add(
            new ImmutableHeader(HttpHeaders.COOKIE, String.join("; ", values))
        );
        return result;
    }

    private static boolean same(final HttpCookie left, final HttpCookie right) {
        return left.getName().equals(right.getName())
            && CookieScope.withoutDot(left.getDomain())
                .equals(CookieScope.withoutDot(right.getDomain()))
            && left.getPath().equals(right.getPath());
    }

    private static boolean expires(final String header) {
        boolean expired = false;
        for (final String attr : CookieJar.SEMICOLON.split(header)) {
            final int pos = attr.indexOf('=');
            if (pos > 0
                && "max-age".equalsIgnoreCase(attr.substring(0, pos).trim())) {
                final String age = attr.substring(pos + 1).trim();
                if (CookieJar.MAX_AGE.matcher(age).matches()) {
                    expired = age.charAt(0) == '-'
                        || age.chars().allMatch(chr -> chr == '0');
                }
            }
        }
        return expired;
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
