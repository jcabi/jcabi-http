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
package com.jcabi.http;

import com.jcabi.aspects.Tv;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.AutoRedirectingWire;
import com.jcabi.http.wire.BasicAuthWire;
import com.jcabi.http.wire.CookieOptimizingWire;
import com.jcabi.xml.XML;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Locale;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.core.HttpHeaders;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for any implementation of {@link Request}.
 *
 * @checkstyle JavadocMethodCheck (500 lines)
 * @since 1.17.8
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.TooManyMethods"})
public abstract class RequestITCaseTemplate {

    /**
     * Type of Request.
     */
    private final Class<? extends Request> type;

    /**
     * Base URI.
     */
    private final URI uri;

    /**
     * Make request for a specific path.
     * @param path Path.
     * @return Request.
     */
    protected final Request request(final String path) {
        return RequestTestTemplate.request(
            this.uri.resolve(path),
            this.type
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {
        HttpURLConnection.HTTP_NOT_FOUND,
        HttpURLConnection.HTTP_OK,
        HttpURLConnection.HTTP_UNAVAILABLE
    })
    final void readsReturnStatusCode(final int code) throws IOException {
        this.request(String.format("/status/%d", code))
            .fetch()
            .as(RestResponse.class)
            .assertStatus(code);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            Request.DELETE,
            Request.GET,
            Request.POST,
            Request.PUT
        }
    )
    final void readsReturnStatusCode(final String method) throws IOException {
        this.request(String.format("/%s", method.toLowerCase(Locale.ENGLISH)))
            .method(method)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    @Test
    final void sendsCookies() throws IOException {
        MatcherAssert.assertThat(
            "Must send cookies",
            this.request("/cookies")
                .header(HttpHeaders.COOKIE, "foo=bar")
                .header(HttpHeaders.COOKIE, "baz=foz")
                .through(CookieOptimizingWire.class)
                .fetch()
                .as(JsonResponse.class)
                .json()
                .readObject()
                .getJsonObject("cookies"),
            Matchers.allOf(
                Matchers.hasEntry(
                    Matchers.is("foo"),
                    Matchers.is(Json.createValue("bar"))
                ),
                Matchers.hasEntry(
                    Matchers.is("baz"),
                    Matchers.is(Json.createValue("foz"))
                )
            )
        );
    }

    @Test
    final void followsLocationHeader() throws IOException {
        this.request("/absolute-redirect/5")
            .through(AutoRedirectingWire.class, Tv.SIX)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    @Test
    final void followsLocationHeaderRelativeRedirect() throws IOException {
        this.request("/relative-redirect/5")
            .through(AutoRedirectingWire.class, Tv.SIX)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

    @Test
    final void failsOnTimeout() {
        Assertions.assertThrows(
            IOException.class,
            () ->
                this.request("/delay/3")
                    .timeout(Tv.THOUSAND, Tv.THOUSAND)
                    .fetch()
        );
    }

    @Test
    final void readsJsonResponse() throws Exception {
        MatcherAssert.assertThat(
            "Must parse Json response",
            this.request("/json")
                .fetch()
                .as(JsonResponse.class)
                .json()
                .readObject(),
            Matchers.notNullValue(JsonObject.class)
        );
    }

    @Test
    @DisabledIf("isJdkRequest")
    final void readsDeflatedJsonResponse() throws Exception {
        MatcherAssert.assertThat(
            "Must undeflate & parse Json response",
            this.request("/deflate")
                .fetch()
                .as(JsonResponse.class)
                .json()
                .readObject(),
            Matchers.hasEntry(
                Matchers.is("deflated"),
                Matchers.is(JsonValue.TRUE)
            )
        );
    }

    @Test
    @DisabledIf("isJdkRequest")
    final void readsGzippedJsonResponse() throws Exception {
        MatcherAssert.assertThat(
            "Must unzip & parse Json response",
            this.request("/gzip")
                .fetch()
                .as(JsonResponse.class)
                .json()
                .readObject(),
            Matchers.hasEntry(
                Matchers.is("gzipped"),
                Matchers.is(JsonValue.TRUE)
            )
        );
    }

    @Test
    final void handlesBasicAuth() throws Exception {
        MatcherAssert.assertThat(
            "Must authenticate with userInfo",
            this.request("/basic-auth/jeff/secret")
                .uri()
                .userInfo("jeff:secret")
                .back()
                .through(BasicAuthWire.class)
                .fetch()
                .as(JsonResponse.class)
                .status(),
            Matchers.is(
                HttpURLConnection.HTTP_OK
            )
        );
    }

    @Test
    final void readsXmlResponse() throws Exception {
        MatcherAssert.assertThat(
            "Must parse XML response",
            this.request("/xml")
                .fetch()
                .as(XmlResponse.class)
                .xml(),
            Matchers.notNullValue(XML.class)
        );
    }

    /**
     * Is JdkRequest being tested?
     * @return True if so.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private boolean isJdkRequest() {
        return JdkRequest.class.equals(this.type);
    }
}
