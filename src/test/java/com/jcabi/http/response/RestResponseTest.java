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
package com.jcabi.http.response;

import com.jcabi.http.Response;
import com.jcabi.http.request.FakeRequest;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
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
            response.cookie("baz"),
            Matchers.allOf(
                Matchers.hasProperty("value", Matchers.equalTo("goo")),
                Matchers.hasProperty("path", Matchers.equalTo("/l"))
            )
        );
        MatcherAssert.assertThat(
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
            new RestResponse(
                new FakeRequest()
                    .uri().set(new URI("http://locahost:888/tt")).back()
                    .fetch()
            ).jump(new URI("/foo/bar?hey")).uri().get(),
            Matchers.hasToString("http://locahost:888/foo/bar?hey")
        );
    }

}
