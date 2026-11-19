/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.ImmutableHeader;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CookieJar}.
 *
 * @since 2.0
 */
final class CookieJarTest {

    /**
     * Home page of the site.
     */
    private static final String HOME = "http://example.com/";

    /**
     * CookieJar sends a cookie back to the host that set it.
     */
    @Test
    void sendsCookieBackToTheSameHost() {
        MatcherAssert.assertThat(
            "should send the cookie to the same host",
            CookieJarTest.cookies(
                CookieJarTest.jar(CookieJarTest.HOME, "sid=abc"),
                "http://example.com/next"
            ),
            Matchers.contains("sid=abc")
        );
    }

    /**
     * CookieJar doesn't send a cookie to another host.
     */
    @Test
    void doesNotSendCookieToAnotherHost() {
        MatcherAssert.assertThat(
            "should not send the cookie to another host",
            CookieJarTest.cookies(
                CookieJarTest.jar(CookieJarTest.HOME, "sid=abc"),
                "http://badexample.com/"
            ),
            Matchers.empty()
        );
    }

    /**
     * CookieJar doesn't send a cookie without a domain to subdomains.
     */
    @Test
    void keepsHostOnlyCookieOnItsHost() {
        MatcherAssert.assertThat(
            "should not send the host-only cookie to a subdomain",
            CookieJarTest.cookies(
                CookieJarTest.jar(CookieJarTest.HOME, "sid=abc"),
                "http://www.example.com/"
            ),
            Matchers.empty()
        );
    }

    /**
     * CookieJar sends a domain cookie to all hosts of the domain.
     */
    @Test
    void sendsDomainCookieToSubdomains() {
        final CookieJar jar = CookieJarTest.jar(
            "http://www.example.com/", "sid=abc; Domain=.Example.com"
        );
        MatcherAssert.assertThat(
            "should send the domain cookie to hosts of the domain only",
            Arrays.asList(
                CookieJarTest.cookies(jar, "http://api.example.com/"),
                CookieJarTest.cookies(jar, CookieJarTest.HOME),
                CookieJarTest.cookies(jar, "http://badexample.com/")
            ),
            Matchers.contains(
                Collections.singletonList("sid=abc"),
                Collections.singletonList("sid=abc"),
                Collections.emptyList()
            )
        );
    }

    /**
     * CookieJar ignores a cookie for a domain the host doesn't belong to.
     */
    @Test
    void ignoresCookieForForeignDomain() {
        MatcherAssert.assertThat(
            "should ignore the cookie for a foreign domain",
            CookieJarTest.cookies(
                CookieJarTest.jar(
                    CookieJarTest.HOME, "sid=abc; Domain=other.com"
                ),
                "http://other.com/"
            ),
            Matchers.empty()
        );
    }

    /**
     * CookieJar ignores a cookie for a public suffix.
     */
    @Test
    void ignoresCookieForPublicSuffix() {
        MatcherAssert.assertThat(
            "should ignore the cookie for a public suffix",
            CookieJarTest.cookies(
                CookieJarTest.jar("http://www.example.co.uk/", "sid=abc; Domain=co.uk"),
                "http://www.example.co.uk/"
            ),
            Matchers.empty()
        );
    }

    /**
     * CookieJar keeps a cookie for a public suffix on the host that set it.
     */
    @Test
    void keepsCookieForPublicSuffixOnItsHost() {
        final CookieJar jar = CookieJarTest.jar(
            "http://github.io/", "sid=abc; Domain=github.io"
        );
        MatcherAssert.assertThat(
            "should keep the cookie for a public suffix on its host",
            Arrays.asList(
                CookieJarTest.cookies(jar, "http://github.io/"),
                CookieJarTest.cookies(jar, "http://evil.github.io/")
            ),
            Matchers.contains(
                Collections.singletonList("sid=abc"),
                Collections.emptyList()
            )
        );
    }

    /**
     * CookieJar doesn't treat IP addresses as domains.
     */
    @Test
    void doesNotMatchDomainOfIpAddress() {
        MatcherAssert.assertThat(
            "should ignore the cookie for a part of an IP address",
            CookieJarTest.cookies(
                CookieJarTest.jar("http://10.0.0.1/", "sid=abc; Domain=0.0.1"),
                "http://10.0.0.1/"
            ),
            Matchers.empty()
        );
    }

    /**
     * CookieJar sends a cookie only to the paths it belongs to.
     */
    @Test
    void sendsCookieToItsPathOnly() {
        final CookieJar jar = CookieJarTest.jar(
            CookieJarTest.HOME, "sid=abc; Path=/admin"
        );
        MatcherAssert.assertThat(
            "should send the cookie to its path only",
            Arrays.asList(
                CookieJarTest.cookies(jar, "http://example.com/admin"),
                CookieJarTest.cookies(jar, "http://example.com/admin/users"),
                CookieJarTest.cookies(jar, "http://example.com/administrator"),
                CookieJarTest.cookies(jar, CookieJarTest.HOME)
            ),
            Matchers.contains(
                Collections.singletonList("sid=abc"),
                Collections.singletonList("sid=abc"),
                Collections.emptyList(),
                Collections.emptyList()
            )
        );
    }

    /**
     * CookieJar prefers the cookie with the most specific path.
     */
    @Test
    void prefersCookieWithLongestPath() {
        final CookieJar jar = CookieJarTest.jar(
            CookieJarTest.HOME, "sid=admin; Path=/admin", "sid=root; Path=/"
        );
        MatcherAssert.assertThat(
            "should send the cookie with the longest path",
            Arrays.asList(
                CookieJarTest.cookies(jar, "http://example.com/admin/users"),
                CookieJarTest.cookies(jar, CookieJarTest.HOME)
            ),
            Matchers.contains(
                Collections.singletonList("sid=admin"),
                Collections.singletonList("sid=root")
            )
        );
    }

    /**
     * CookieJar uses the directory of the URI as a default path.
     */
    @Test
    void usesDirectoryOfUriAsDefaultPath() {
        final CookieJar jar = CookieJarTest.jar(
            "http://example.com/shop/cart/login", "sid=abc"
        );
        MatcherAssert.assertThat(
            "should send the cookie to the directory of the URI only",
            Arrays.asList(
                CookieJarTest.cookies(jar, "http://example.com/shop/cart/pay"),
                CookieJarTest.cookies(jar, "http://example.com/shop/help")
            ),
            Matchers.contains(
                Collections.singletonList("sid=abc"),
                Collections.emptyList()
            )
        );
    }

    /**
     * CookieJar sends a secure cookie over HTTPS only.
     */
    @Test
    void sendsSecureCookieOverHttpsOnly() {
        final CookieJar jar = CookieJarTest.jar(
            "https://example.com/", "sid=abc; Secure"
        );
        MatcherAssert.assertThat(
            "should send the secure cookie over HTTPS only",
            Arrays.asList(
                CookieJarTest.cookies(jar, "https://example.com/"),
                CookieJarTest.cookies(jar, CookieJarTest.HOME)
            ),
            Matchers.contains(
                Collections.singletonList("sid=abc"),
                Collections.emptyList()
            )
        );
    }

    /**
     * CookieJar forgets a cookie when it is expired by Max-Age.
     */
    @Test
    void forgetsCookieExpiredByMaxAge() {
        final CookieJar jar = CookieJarTest.jar(
            CookieJarTest.HOME, "sid=abc", "theme=dark"
        );
        jar.store(
            URI.create(CookieJarTest.HOME),
            Collections.singletonMap(
                HttpHeaders.SET_COOKIE,
                Collections.singletonList("sid=; Max-Age=0")
            )
        );
        MatcherAssert.assertThat(
            "should forget the cookie expired by Max-Age",
            CookieJarTest.cookies(jar, CookieJarTest.HOME),
            Matchers.contains("theme=dark")
        );
    }

    /**
     * CookieJar forgets a cookie when it is expired by Expires.
     */
    @Test
    void forgetsCookieExpiredByDate() {
        final CookieJar jar = CookieJarTest.jar(CookieJarTest.HOME, "sid=abc");
        jar.store(
            URI.create(CookieJarTest.HOME),
            Collections.singletonMap(
                HttpHeaders.SET_COOKIE,
                Collections.singletonList(
                    "sid=; Expires=Thu, 01 Jan 1970 00:00:00 GMT"
                )
            )
        );
        MatcherAssert.assertThat(
            "should forget the cookie expired by date",
            CookieJarTest.cookies(jar, CookieJarTest.HOME),
            Matchers.empty()
        );
    }

    /**
     * CookieJar replaces a cookie with a newer one.
     */
    @Test
    void replacesCookieWithNewerOne() {
        MatcherAssert.assertThat(
            "should send the latest value of the cookie",
            CookieJarTest.cookies(
                CookieJarTest.jar(CookieJarTest.HOME, "sid=old", "sid=new"),
                CookieJarTest.HOME
            ),
            Matchers.contains("sid=new")
        );
    }

    /**
     * CookieJar ignores malformed cookies.
     */
    @Test
    void ignoresMalformedCookies() {
        MatcherAssert.assertThat(
            "should ignore the malformed cookie",
            CookieJarTest.cookies(
                CookieJarTest.jar(CookieJarTest.HOME, "=abc", "sid=abc"),
                CookieJarTest.HOME
            ),
            Matchers.contains("sid=abc")
        );
    }

    /**
     * CookieJar finds Set-Cookie headers regardless of their case.
     */
    @Test
    void readsSetCookieHeaderInAnyCase() {
        final CookieJar jar = new CookieJar();
        jar.store(
            URI.create(CookieJarTest.HOME),
            Collections.singletonMap(
                "set-cookie", Collections.singletonList("sid=abc")
            )
        );
        MatcherAssert.assertThat(
            "should read the lower-case Set-Cookie header",
            CookieJarTest.cookies(jar, CookieJarTest.HOME),
            Matchers.contains("sid=abc")
        );
    }

    /**
     * CookieJar merges received cookies with cookies of the request.
     */
    @Test
    void mergesCookiesWithRequestCookies() {
        MatcherAssert.assertThat(
            "should merge all cookies into one header",
            CookieJarTest.jar(CookieJarTest.HOME, "sid=new", "lang=en").attach(
                URI.create(CookieJarTest.HOME),
                Arrays.asList(
                    new ImmutableHeader(HttpHeaders.ACCEPT, "text/plain"),
                    new ImmutableHeader(HttpHeaders.COOKIE, "sid=old; theme=dark"),
                    new ImmutableHeader(HttpHeaders.COOKIE, "flag")
                )
            ),
            Matchers.containsInAnyOrder(
                new ImmutableHeader(HttpHeaders.ACCEPT, "text/plain"),
                new ImmutableHeader(
                    HttpHeaders.COOKIE, "sid=new; theme=dark; flag; lang=en"
                )
            )
        );
    }

    /**
     * CookieJar leaves headers untouched when there is nothing to send.
     */
    @Test
    void leavesHeadersUntouchedWithoutCookies() {
        final Collection<Map.Entry<String, String>> headers =
            Collections.singletonList(
                new ImmutableHeader(HttpHeaders.COOKIE, "sid=old")
            );
        MatcherAssert.assertThat(
            "should return the same headers",
            new CookieJar().attach(URI.create(CookieJarTest.HOME), headers),
            Matchers.sameInstance(headers)
        );
    }

    private static CookieJar jar(final String uri, final String... cookies) {
        final CookieJar jar = new CookieJar();
        for (final String cookie : cookies) {
            jar.store(
                URI.create(uri),
                Collections.singletonMap(
                    HttpHeaders.SET_COOKIE, Collections.singletonList(cookie)
                )
            );
        }
        return jar;
    }

    private static List<String> cookies(final CookieJar jar, final String uri) {
        return jar.attach(URI.create(uri), Collections.emptyList()).stream()
            .filter(header -> HttpHeaders.COOKIE.equals(header.getKey()))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }
}
