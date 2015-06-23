/**
 * Copyright (c) 2011-2015, jcabi.com
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

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import com.jcabi.http.wire.BasicAuthWire;
import com.jcabi.http.wire.UserAgentWire;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Test case for {@link Request} and its implementations.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@SuppressWarnings("PMD.TooManyMethods")
@RunWith(Parameterized.class)
public final class RequestTest {

    /**
     * Type of request.
     */
    private final transient Class<? extends Request> type;

    /**
     * Public ctor.
     * @param req Request type
     */
    public RequestTest(final Class<? extends Request> req) {
        this.type = req;
    }

    /**
     * Parameters.
     * @return Array of args
     */
    @Parameterized.Parameters
    public static Collection<Object[]> primeNumbers() {
        return Arrays.asList(
            new Object[]{ApacheRequest.class},
            new Object[]{JdkRequest.class}
        );
    }

    /**
     * BaseRequest can fetch HTTP request and process HTTP response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("\u20ac! hello!")
        ).start();
        this.request(container.home())
            .uri().path("/helloall").back()
            .method(Constants.GET)
            .fetch().as(RestResponse.class)
            .assertBody(Matchers.containsString("\u20ac!"))
            .assertBody(Matchers.containsString("hello!"))
            .assertStatus(HttpURLConnection.HTTP_OK);
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.uri(),
            Matchers.hasToString(Matchers.containsString("helloall"))
        );
        MatcherAssert.assertThat(
            query.method(),
            Matchers.equalTo(Constants.GET)
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch HTTP headers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestWithHeaders() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        this.request(container.home())
            .through(UserAgentWire.class)
            .uri().path("/foo1").back()
            .method(Constants.GET)
            .header(HttpHeaders.ACCEPT, "*/*")
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers(),
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
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch GET request with query params.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsTextWithGetParameters() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "some value of this param &^%*;'\"\u20ac\"";
        this.request(container.home())
            .uri().queryParam("q", value).back()
            .method(Constants.GET)
            .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            URLDecoder.decode(query.uri().toString(), Constants.ENCODING),
            Matchers.endsWith("\"€\"")
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch body with HTTP POST request.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsTextWithPostRequestMatchParam() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "some random value of \u20ac param \"&^%*;'\"";
        this.request(container.home())
            .method(Constants.POST)
            .body().formParam("p", value).back()
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            URLDecoder.decode(query.body(), Constants.ENCODING),
            Matchers.containsString(value)
        );
        container.stop();
    }

    /**
     * BaseRequest can fetch body with HTTP POST request.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsTextWithPostRequestMatchBody() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "\u20ac some body value with \"&^%*;'\"";
        this.request(container.home())
            .method(Constants.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .body().set(URLEncoder.encode(value, Constants.ENCODING)).back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            URLDecoder.decode(query.body(), Constants.ENCODING),
            Matchers.containsString(value)
        );
        container.stop();
    }

    /**
     * BaseRequest can assert HTTP status code value.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpStatus() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_FOUND, "")
        ).start();
        this.request(container.home())
            .method(Constants.GET)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_NOT_FOUND)
            .assertStatus(
                Matchers.equalTo(HttpURLConnection.HTTP_NOT_FOUND)
            );
        container.stop();
    }

    /**
     * BaseRequest can assert response body.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpResponseBody() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("some text \u20ac")
        ).start();
        this.request(container.home())
            .method(Constants.GET)
            .fetch().as(RestResponse.class)
            .assertBody(Matchers.containsString("text \u20ac"))
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
    }

    /**
     * BaseRequest can assert HTTP headers in response.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsHttpHeaders() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("").withHeader(
                HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN
            )
        ).start();
        this.request(container.home())
            .method(Constants.GET)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertHeader(
                "absent-for-sure",
                Matchers.emptyIterableOf(String.class)
            )
            .assertHeader(
                HttpHeaders.CONTENT_TYPE,
                Matchers.<String>everyItem(
                    Matchers.containsString(MediaType.TEXT_PLAIN)
                )
            );
        container.stop();
    }

    /**
     * BaseRequest can assert response body content with XPath query.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void assertsResponseBodyWithXpathQuery() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("<root><a>\u0443\u0440\u0430!</a></root>")
        ).start();
        this.request(container.home())
            .method(Constants.GET)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/root/a[contains(.,'!')]");
        container.stop();
    }

    /**
     * BaseRequest can work with URL returned by ContainerMocker.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void mockedUrlIsInCorrectFormat() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        container.stop();
        final URI uri = container.home();
        MatcherAssert.assertThat(
            uri.toString().matches("^http://localhost:\\d+/$"),
            Matchers.describedAs(uri.toString(), Matchers.is(true))
        );
    }

    /**
     * BaseRequest can handle unicode in plain text response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void acceptsUnicodeInPlainText() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("\u0443\u0440\u0430!").withHeader(
                HttpHeaders.CONTENT_TYPE, "text/plain;charset=utf-8"
            )
        ).start();
        this.request(container.home())
            .method(Constants.GET)
            .uri().path("/abcdefff").back()
            .fetch().as(RestResponse.class)
            .assertBody(Matchers.containsString("\u0443\u0440\u0430"))
            .assertBody(Matchers.containsString("!"));
        container.stop();
    }

    /**
     * BaseRequest can handle unicode in XML response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void acceptsUnicodeInXml() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("<text>\u0443\u0440\u0430!</text>").withHeader(
                HttpHeaders.CONTENT_TYPE, "text/xml;charset=utf-8"
            )
        ).start();
        this.request(container.home())
            .method(Constants.GET)
            .uri().path("/barbar").back()
            .fetch().as(XmlResponse.class)
            .assertXPath("/text[contains(.,'\u0443\u0440\u0430')]");
        container.stop();
    }

    /**
     * BaseRequest can use basic authentication scheme.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsBasicAuthenticationHeader() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final URI uri = UriBuilder.fromUri(container.home())
            .userInfo("user:\u20ac\u20ac").build();
        this.request(uri)
            .through(BasicAuthWire.class)
            .method(Constants.GET)
            .uri().path("/abcde").back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers(),
            Matchers.hasEntry(
                Matchers.equalTo(HttpHeaders.AUTHORIZATION),
                Matchers.hasItem("Basic dXNlcjolRTIlODIlQUMlRTIlODIlQUM=")
            )
        );
    }

    /**
     * BaseRequest can fetch GET request twice.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsIdenticalHttpRequestTwice() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .start();
        final Request req = this.request(container.home())
            .uri().path("/foo-X").back()
            .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_XML);
        req.method(Constants.GET).fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        req.method(Constants.POST).fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        req.method(Constants.GET).fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            container.take().uri().toString(),
            Matchers.endsWith("foo-X")
        );
    }

    /**
     * BaseRequest can return redirect status (without redirecting).
     * @throws Exception If something goes wrong inside
     * @since 0.10
     */
    @Test
    public void doesntRedirectWithoutRequest() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withStatus(HttpURLConnection.HTTP_SEE_OTHER)
                .withHeader(HttpHeaders.LOCATION, "http://www.google.com")
        ).start();
        this.request(container.home())
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_SEE_OTHER);
        container.stop();
    }

    /**
     * BaseRequest can fetch body with HTTP POST request.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsRequestBodyAsInputStream() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final String value = "\u20ac body as stream \"&^%*;'\"";
        final ByteArrayInputStream stream =
            new ByteArrayInputStream(value.getBytes(Constants.CHARSET));
        this.request(container.home())
            .method(Constants.POST)
            .header(
                HttpHeaders.CONTENT_TYPE,
                MediaType.APPLICATION_FORM_URLENCODED
            )
            .fetch(stream).as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.body(),
            Matchers.containsString(value)
        );
        container.stop();
    }

    /**
     * BaseRequest.fetch(InputStream) throws an exception if the body has been
     * previously set.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IllegalStateException.class)
    public void fetchThrowsExceptionWhenBodyIsNotEmpty() throws Exception {
        this.request(new URI("http://localhost:78787"))
            .method(Constants.GET)
            .body().set("already set").back()
            .fetch(new ByteArrayInputStream("ba".getBytes(Constants.CHARSET)));
    }

    /**
     * Make a request.
     * @param uri URI to start with
     * @return Request
     * @throws Exception If fails
     */
    private Request request(final URI uri) throws Exception {
        return this.type.getDeclaredConstructor(URI.class).newInstance(uri);
    }

}
