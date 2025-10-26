/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.response.JsonResponse;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import jakarta.json.Json;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Integration case for {@link com.jcabi.http.request.ApacheRequest}.
 * @since 1.1
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
final class RequestITCase extends RequestTestTemplate {

    /**
     * BaseRequest can fetch HTTP request and process HTTP response.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    @Timeout(10)
    @DisabledOnOs(OS.WINDOWS)
    void sendsHttpRequestAndProcessesHttpResponse(
        final Class<? extends Request> type
    ) throws Exception {
        RequestTestTemplate.request(new URI("https://www.rt.com/rss/"), type)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/rss/channel");
    }

    /**
     * BaseRequest can process not-OK response.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    @Timeout(10)
    void processesNotOkHttpResponse(
        final Class<? extends Request> type
    ) throws Exception {
        RequestTestTemplate.request(new URI("https://badssl.com/404"), type)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * BaseRequest can throw a correct exception on connection error.
     * @param type Request type
     */
    @Values
    @ParameterizedTest
    void continuesOnConnectionError(final Class<? extends Request> type) {
        Assertions.assertThrows(
            IOException.class,
            new Executable() {
                @Override
                public void execute() throws Throwable {
                    RequestTestTemplate.request(
                        new URI("http://localhost:6868/"),
                        type
                    ).method(Request.GET).fetch();
                }
            }
        );
    }

    @Test
    void handlesGet() throws Exception {
        try (MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    Json.createObjectBuilder().toString()
                )
            ).start()) {
            new ApacheRequest(container.home())
                .method(Request.GET)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
            final MkQuery query = container.take();
            MatcherAssert.assertThat(
                "should be 'GET'",
                query.method(),
                Matchers.is("GET")
            );
        }
    }

    @Test
    void handlesDelete() throws Exception {
        try (MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    Json.createObjectBuilder().toString()
                )
            ).start()) {
            new ApacheRequest(container.home())
                .method(Request.DELETE)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
            MatcherAssert.assertThat(
                "should be 'DELETE'",
                container.take().method(),
                Matchers.is("DELETE")
            );
        }
    }

    @Test
    void handlesDeleteWithBody() throws Exception {
        try (MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    Json.createObjectBuilder().toString()
                )
            ).start()) {
            new ApacheRequest(container.home())
                .method(Request.DELETE)
                .body().set("{}").back()
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .as(JsonResponse.class)
                .json();
            final MkQuery take = container.take();
            MatcherAssert.assertThat(
                "should be 'DELETE'",
                take.method(),
                Matchers.is("DELETE")
            );
            MatcherAssert.assertThat(
                "should be '{}'",
                take.body(),
                Matchers.is("{}")
            );
        }
    }
}
