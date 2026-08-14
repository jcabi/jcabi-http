/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.google.common.base.Joiner;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.mock.MkQueryMatchers;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.BasicAuthWire;
import com.jcabi.http.wire.UserAgentWire;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.glassfish.grizzly.http.server.Constants;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Test case for {@link Request} and its implementations.
 * @since 1.7
 */
@SuppressWarnings("PMD.TooManyMethods")
final class RequestTest extends RequestTestTemplate {

    /**
     * BaseRequest can fetch HTTP request and process HTTP response.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsHttpRequestAndProcessesHttpResponse(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("\u20ac! hello!")
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .uri().path("/helloall").back()
                .method(Request.GET)
                .fetch().as(RestResponse.class);
        final MkQuery query = container.take();
        container.stop();
        Assertions.assertAll(
            () -> response.assertBody(
                Matchers.allOf(
                    Matchers.containsString("\u20ac!"),
                    Matchers.containsString("hello!")
                )
            ),
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should contains 'helloall'",
                query,
                MkQueryMatchers.hasPath(Matchers.containsString("helloall"))
            ),
            () -> MatcherAssert.assertThat(
                "should be GET method",
                query.method(),
                Matchers.equalTo(Request.GET)
            )
        );
    }

    /**
     * BaseRequest can fetch HTTP headers.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsHttpRequestWithHeaders(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .through(UserAgentWire.class)
                .uri().path("/foo1").back()
                .method(Request.GET)
                .header(HttpHeaders.ACCEPT, "*/*")
                .fetch().as(RestResponse.class);
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should be accept '*' and user-agent 'jcabi'",
                container.take().headers(),
                Matchers.allOf(
                    Matchers.hasEntry(
                        Matchers.equalTo(HttpHeaders.ACCEPT),
                        Matchers.hasItem(Matchers.containsString("*"))
                    ),
                    Matchers.hasEntry(
                        Matchers.equalTo(HttpHeaders.USER_AGENT),
                        Matchers.hasItem(Matchers.containsString("jcabi"))
                    )
                )
            )
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch GET request with query params.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsTextWithGetParameters(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .uri()
                .queryParam("q", "some value of this param &^%*;'\"\u20ac\"")
                .back()
                .method(Request.GET)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
                .fetch().as(RestResponse.class);
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should be ends with euro sign",
                URLDecoder.decode(
                    container.take().uri().toString(),
                    String.valueOf(StandardCharsets.UTF_8)
                ),
                Matchers.endsWith("\"\u20ac\"")
            )
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch body with HTTP POST request.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsTextWithPostRequestMatchParam(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "some random value of \u20ac param \"&^%*;'\"";
        RequestTestTemplate.request(container.home(), type)
            .method(Request.POST)
            .body().formParam("p", value).back()
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .fetch();
        MatcherAssert.assertThat(
            "should be with param",
            URLDecoder.decode(
                container.take().body(),
                StandardCharsets.UTF_8.toString()
            ),
            Matchers.is(String.format("p=%s", value))
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch body with HTTP POST request with params.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsTextWithPostRequestMatchMultipleParams(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "some value of \u20ac param \"&^%*;'\"";
        final String follow = "other value of \u20ac param \"&^%*;'\"";
        RequestTestTemplate.request(container.home(), type)
            .method(Request.POST)
            .body()
            .formParam("a", value)
            .formParam("b", follow)
            .back()
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .fetch();
        MatcherAssert.assertThat(
            "should be with multiple params",
            URLDecoder.decode(
                container.take().body(),
                StandardCharsets.UTF_8.toString()
            ),
            Matchers.is(String.format("a=%s&b=%s", value, follow))
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch multipart body with HTTP POST request
     * with single byte param.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     * @checkstyle LineLength (36 lines)
     */
    @Values
    @ParameterizedTest
    void sendsMultipartPostRequestMatchByteParam(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .method(Request.POST)
                .header(
                    HttpHeaders.CONTENT_TYPE,
                    String.format(
                        "%s; boundary=--xx", MediaType.MULTIPART_FORM_DATA
                    )
                )
                .multipartBody()
                .formParam("x", new byte[] {Byte.parseByte("-122")})
                .back()
                .fetch().as(RestResponse.class);
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should be match byte param",
                container.take().body(),
                Matchers.is(
                    Joiner.on(Constants.CRLF).join(
                        "----xx",
                        "Content-Disposition: form-data; name=\"x\"; filename=\"binary\"",
                        RequestTest.steamContentType(),
                        "",
                        "\ufffd",
                        "----xx--"
                    )
                )
            )
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch multipart body with HTTP POST request
     * with single param.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     * @checkstyle LineLength (37 lines)
     */
    @Values
    @ParameterizedTest
    void sendsMultipartPostRequestMatchSingleParam(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "value of \u20ac part param \"&^%*;'\"";
        RequestTestTemplate.request(container.home(), type)
            .method(Request.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                String.format(
                    "%s; boundary=--xyz", MediaType.MULTIPART_FORM_DATA
                )
            )
            .multipartBody()
            .formParam("c", value)
            .back()
            .fetch();
        MatcherAssert.assertThat(
            "should be match single param",
            container.take().body(),
            Matchers.is(
                Joiner.on(Constants.CRLF).join(
                    "----xyz",
                    "Content-Disposition: form-data; name=\"c\"; filename=\"binary\"",
                    RequestTest.steamContentType(),
                    "",
                    value,
                    "----xyz--"
                )
            )
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch multipart body with HTTP POST request
     * with two params.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     * @checkstyle LineLength (44 lines)
     */
    @Values
    @ParameterizedTest
    void sendsMultipartPostRequestMatchTwoParams(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "value of \u20ac one param \"&^%*;'\"";
        final String other = "value of \u20ac two param \"&^%*;'\"";
        final String separator = "--xy--";
        RequestTestTemplate.request(container.home(), type)
            .method(Request.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                String.format(
                    "%s; boundary=xy--", MediaType.MULTIPART_FORM_DATA
                )
            )
            .multipartBody()
            .formParam("d", value)
            .formParam("e", other)
            .back()
            .fetch();
        MatcherAssert.assertThat(
            "should be match two params",
            container.take().body(),
            Matchers.is(
                Joiner.on(Constants.CRLF).join(
                    separator,
                    "Content-Disposition: form-data; name=\"d\"; filename=\"binary\"",
                    RequestTest.steamContentType(),
                    "",
                    value,
                    separator,
                    "Content-Disposition: form-data; name=\"e\"; filename=\"binary\"",
                    RequestTest.steamContentType(),
                    "",
                    other,
                    "--xy----"
                )
            )
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch body with HTTP POST request.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsTextWithPostRequestMatchBody(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "\u20ac some body value with \"&^%*;'\"";
        RequestTestTemplate.request(container.home(), type)
            .method(Request.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .body()
            .set(URLEncoder.encode(value, StandardCharsets.UTF_8.toString()))
            .back()
            .fetch();
        MatcherAssert.assertThat(
            "should be match body",
            URLDecoder.decode(
                container.take().body(),
                StandardCharsets.UTF_8.toString()
            ),
            Matchers.containsString(value)
        );
        container.stop();
    }

    /**
     * BaseRequest can assert HTTP status code value.
     * @param type Request type
     * @throws Exception If something goes wrong inside.
     */
    @Values
    @ParameterizedTest
    void assertsHttpStatus(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_FOUND, "")
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .method(Request.GET)
                .fetch().as(RestResponse.class);
        container.stop();
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_NOT_FOUND),
            () -> response.assertStatus(
                Matchers.equalTo(HttpURLConnection.HTTP_NOT_FOUND)
            )
        );
    }

    /**
     * BaseRequest can assert response body.
     * @param type Request type
     * @throws Exception If something goes wrong inside.
     */
    @Values
    @ParameterizedTest
    void assertsHttpResponseBody(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("some text \u20ac")
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .method(Request.GET)
                .fetch().as(RestResponse.class);
        container.stop();
        Assertions.assertAll(
            () -> response.assertBody(
                Matchers.containsString("text \u20ac")
            ),
            () -> response.assertStatus(HttpURLConnection.HTTP_OK)
        );
    }

    /**
     * BaseRequest can assert HTTP headers in response.
     * @param type Request type
     * @throws Exception If something goes wrong inside.
     */
    @Values
    @ParameterizedTest
    void assertsHttpHeaders(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("").withHeader(
                HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN
            )
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .method(Request.GET)
                .fetch().as(RestResponse.class);
        container.stop();
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> response.assertHeader(
                "absent-for-sure",
                Matchers.emptyIterableOf(String.class)
            ),
            () -> response.assertHeader(
                HttpHeaders.CONTENT_TYPE,
                Matchers.everyItem(
                    Matchers.containsString(MediaType.TEXT_PLAIN)
                )
            )
        );
    }

    /**
     * BaseRequest can assert response body content with XPath query.
     * @param type Request type
     * @throws Exception If something goes wrong inside.
     */
    @Values
    @ParameterizedTest
    void assertsResponseBodyWithXpathQuery(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("<root><a>\u0443\u0440\u0430!</a></root>")
        ).start();
        final RestResponse response =
            RequestTestTemplate.request(container.home(), type)
                .method(Request.GET)
                .fetch().as(RestResponse.class);
        container.stop();
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> response.as(XmlResponse.class)
                .assertXPath("/root/a[contains(.,'!')]")
        );
    }

    /**
     * BaseRequest can work with URL returned by ContainerMocker.
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void mockedUrlIsInCorrectFormat() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        container.stop();
        final URI uri = container.home();
        MatcherAssert.assertThat(
            "should be correct URI",
            uri.toString().matches("^http://localhost:\\d+/$"),
            Matchers.describedAs(uri.toString(), Matchers.is(true))
        );
    }

    /**
     * BaseRequest can handle unicode in plain text response.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void acceptsUnicodeInPlainText(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("\u0443\u0440\u0430!").withHeader(
                HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8"
            )
        ).start();
        RequestTestTemplate.request(container.home(), type)
            .method(Request.GET)
            .uri().path("/abcdefff").back()
            .fetch().as(RestResponse.class)
            .assertBody(
                Matchers.allOf(
                    Matchers.containsString("\u0443\u0440\u0430"),
                    Matchers.containsString("!")
                )
            );
        container.stop();
    }

    /**
     * BaseRequest can handle unicode in XML response.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void acceptsUnicodeInXml(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("<text>\u0443\u0440\u0430!</text>").withHeader(
                HttpHeaders.CONTENT_TYPE, "text/xml;charset=utf-8"
            )
        ).start();
        RequestTestTemplate.request(container.home(), type)
            .method(Request.GET)
            .uri().path("/barbar").back()
            .fetch().as(XmlResponse.class)
            .assertXPath("/text[contains(.,'\u0443\u0440\u0430')]");
        container.stop();
    }

    /**
     * BaseRequest can use basic authentication scheme.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void sendsBasicAuthenticationHeader(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final RestResponse response = RequestTestTemplate.request(
            UriBuilder.fromUri(container.home())
                .userInfo("user:\u20ac\u20ac").build(),
            type
        )
            .through(BasicAuthWire.class)
            .method(Request.GET)
            .uri().path("/abcde").back()
            .fetch().as(RestResponse.class);
        container.stop();
        Assertions.assertAll(
            () -> response.assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should be basic authorization",
                container.take().headers(),
                Matchers.hasEntry(
                    Matchers.equalTo(HttpHeaders.AUTHORIZATION),
                    Matchers.hasItem("Basic dXNlcjrigqzigqw=")
                )
            )
        );
    }

    /**
     * BaseRequest can fetch GET request twice.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsIdenticalHttpRequestTwice(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .start();
        final Request req = RequestTestTemplate.request(container.home(), type)
            .uri().path("/foo-X").back()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        Assertions.assertAll(
            () -> req.method(Request.GET).fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK),
            () -> req.method(Request.POST).fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK),
            () -> req.method(Request.GET).fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK),
            () -> MatcherAssert.assertThat(
                "should be ends with 'foo-X'",
                container.take(),
                MkQueryMatchers.hasPath(Matchers.endsWith("foo-X"))
            )
        );
        container.stop();
    }

    /**
     * BaseRequest can return redirect status (without redirecting).
     * @param type Request type
     * @throws Exception If something goes wrong inside
     * @since 0.10
     */
    @Values
    @ParameterizedTest
    void doesntRedirectWithoutRequest(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withStatus(HttpURLConnection.HTTP_SEE_OTHER)
                .withHeader(HttpHeaders.LOCATION, "http://www.google.com")
        ).start();
        RequestTestTemplate.request(container.home(), type)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        container.stop();
    }

    /**
     * BaseRequest can fetch body with HTTP POST request.
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void sendsRequestBodyAsInputStream(
        final Class<? extends Request> type
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "\u20ac body as stream \"&^%*;'\"";
        RequestTestTemplate.request(container.home(), type)
            .method(Request.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .fetch(
                new ByteArrayInputStream(
                    value.getBytes(StandardCharsets.UTF_8)
                )
            );
        MatcherAssert.assertThat(
            "should contains body as input stream",
            container.take().body(),
            Matchers.containsString(value)
        );
        container.stop();
    }

    /**
     * BaseRequest.fetch(InputStream) throws an exception if the body has been
     * previously set.
     * @param type Request type
     */
    @Values
    @ParameterizedTest
    void fetchThrowsExceptionWhenBodyIsNotEmpty(
        final Class<? extends Request> type
    ) {
        Assertions.assertThrows(
            IllegalStateException.class,
            () -> RequestTestTemplate.request(
                new URI("http://localhost:78787"),
                type
            )
                .method(Request.GET)
                .body().set("already set").back()
                .fetch(
                    new ByteArrayInputStream(
                        "hello".getBytes(StandardCharsets.UTF_8)
                    )
                )
        );
    }

    /**
     * RestResponse.assertBody matches only the response body, not HTTP headers.
     * Reproduces https://github.com/jcabi/jcabi-http/issues/177
     * @param type Request type
     * @throws Exception If something goes wrong inside
     */
    @Values
    @ParameterizedTest
    void assertBodyMatchesOnlyBodyNotHttpHeaders(
        final Class<? extends Request> type
    ) throws Exception {
        final String expected = "hello";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(expected)
        ).start();
        try {
            RequestTestTemplate.request(container.home(), type)
                .method(Request.POST)
                .body().set("request").back()
                .fetch().as(RestResponse.class)
                .assertBody(
                    Matchers.equalTo(expected)
                );
        } finally {
            container.stop();
        }
    }

    /**
     * Content type stream.
     * @return Content type header.
     */
    private static String steamContentType() {
        return "Content-Type: application/octet-stream";
    }
}
