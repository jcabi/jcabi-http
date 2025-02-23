/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.Response;
import com.jcabi.http.request.FakeRequest;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.HttpURLConnection;
import java.net.URI;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test case for {@link RestResponse}.
 * @since 1.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class RestResponseTest {

    /**
     * RestResponse can assert HTTP status.
     */
    @Test
    void assertsHttpStatusCode() {
        Assertions.assertThrows(
            AssertionError.class,
            new Executable() {
                @Override
                public void execute() throws Throwable {
                    new RestResponse(
                        new FakeRequest()
                            .withStatus(HttpURLConnection.HTTP_OK)
                            .fetch()
                    ).assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
                }
            }
        );
    }

    /**
     * RestResponse can assert HTTP header.
     * @throws Exception If something goes wrong inside
     */
    @Test
    @SuppressWarnings("unchecked")
    void assertsHttpHeaders() throws Exception {
        final String name = "Abc";
        final String value = "t66";
        final Response rsp = new FakeRequest().withHeader(name, value).fetch();
        new RestResponse(rsp).assertHeader(
            name,
            Matchers.allOf(
                Matchers.hasItems(value),
                Matcher.class.cast(Matchers.hasSize(1))
            )
        );
        new RestResponse(rsp).assertHeader(
            "Something-Else-Which-Is-Absent",
            Matcher.class.cast(Matchers.empty())
        );
    }

    /**
     * RestResponse can retrieve a cookie by name.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void retrievesCookieByName() throws Exception {
        final RestResponse response = new RestResponse(
            new FakeRequest()
                .withBody("<hello/>")
                .withHeader(
                    HttpHeaders.SET_COOKIE,
                    "cookie1=foo1;Path=/;Comment=\"\", bar=1;"
                )
                .fetch()
        );
        MatcherAssert.assertThat(
            "should contains value & path",
            response.cookie("cookie1"),
            Matchers.allOf(
                Matchers.hasProperty("value", Matchers.equalTo("foo1")),
                Matchers.hasProperty("path", Matchers.equalTo("/"))
            )
        );
    }

    /**
     * RestResponse can retrieve a cookie by name if header occurs several
     * times.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void retrievesCookieByNameSeveralValues() throws Exception {
        final RestResponse response = new RestResponse(
            new FakeRequest()
                .withHeader(HttpHeaders.SET_COOKIE, "foo=bar; path=/i;")
                .withHeader(HttpHeaders.SET_COOKIE, "baz=goo; path=/l;")
                .fetch()
        );
        MatcherAssert.assertThat(
            "should contains value & path",
            response.cookie("baz"),
            Matchers.allOf(
                Matchers.hasProperty("value", Matchers.equalTo("goo")),
                Matchers.hasProperty("path", Matchers.equalTo("/l"))
            )
        );
        MatcherAssert.assertThat(
            "should contains value & path",
            response.cookie("foo"),
            Matchers.allOf(
                Matchers.hasProperty("value", Matchers.equalTo("bar")),
                Matchers.hasProperty("path", Matchers.equalTo("/i"))
            )
        );
    }

    /**
     * RestResponse can jump to a relative URL.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void jumpsToRelativeUrls() throws Exception {
        MatcherAssert.assertThat(
            "should contains value & path",
            new RestResponse(
                new FakeRequest()
                    .uri().set(new URI("http://locahost:888/tt")).back()
                    .fetch()
            ).jump(new URI("/foo/bar?hey")).uri().get(),
            Matchers.hasToString("http://locahost:888/foo/bar?hey")
        );
    }

}
