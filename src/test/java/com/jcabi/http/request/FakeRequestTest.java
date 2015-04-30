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
package com.jcabi.http.request;

import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.CharEncoding;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link FakeRequest}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class FakeRequestTest {

    /**
     * FakeRequest can fetch a fake response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        new FakeRequest()
            .withStatus(HttpURLConnection.HTTP_OK)
            .withReason("OK")
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .withBody("how are you?")
            .uri().path("/helloall").back()
            .method(Request.POST)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .assertBody(Matchers.containsString("are you?"));
    }

    /**
     * FakeRequest can change URI.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void changesUri() throws Exception {
        MatcherAssert.assertThat(
            new FakeRequest()
                .uri().set(new URI("http://facebook.com")).back()
                .uri().get().toString(),
            Matchers.endsWith("facebook.com/")
        );
    }

    /**
     * FakeRequest can change URI in response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void changesUriInResponse() throws Exception {
        MatcherAssert.assertThat(
            new FakeRequest()
                .uri().set(new URI("http://google.com")).back()
                .fetch().back()
                .uri().get().toString(),
            Matchers.containsString("google.com")
        );
    }

    /**
     * FakeRequest.fetch(InputStream) throws an exception if a non-empty body
     * has been previously set.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IllegalStateException.class)
    public void fetchThrowsExceptionWhenBodyIsNotEmpty() throws Exception {
        new FakeRequest()
            .withStatus(HttpURLConnection.HTTP_OK)
            .withBody("blah")
            .fetch(
                new ByteArrayInputStream("foo".getBytes(CharEncoding.UTF_8))
            );
    }

    /**
     * FakeRequest returns the Response Body if the Request Body is set.
     * @throws Exception If something goes wrong inside.
     * @see https://github.com/jcabi/jcabi-http/issues/47
     */
    @Test
    public void fakeRequestReturnsResponseBody() throws Exception {
        final String response = "the response body";
        final String request = "the request body";
        new FakeRequest().withBody(response)
            .body().set(request).back()
            .fetch()
            .as(RestResponse.class)
            .assertBody(
                Matchers.allOf(
                    Matchers.is(response),
                    Matchers.not(Matchers.is(request))
                )
            );
    }

    /**
     * FakeRequest can identify itself uniquely.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void identifiesUniquely() throws Exception {
        MatcherAssert.assertThat(
            new FakeRequest().header("header-1", "value-1"),
            Matchers.not(
                Matchers.equalTo(
                    new FakeRequest().header("header-2", "value-2")
                )
            )
        );
        MatcherAssert.assertThat(
            new FakeRequest(),
            Matchers.equalTo(new FakeRequest())
        );
    }

    /**
     * FakeRequest can return matching Response body.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void returnsMatchingResponseBody() throws Exception {
        final String[] responses = {"the response 1", "the response 2"};
        final String[] urls = {"^/first.*", "^/second.*"};
        final String request = "the request";
        final Request req =  new FakeRequest()
            .withBody(urls[0], responses[0])
            .withBody(urls[1], responses[1]);
        req.uri().set(new URI("/first/path")).back()
            .body().set(request).back()
            .fetch()
            .as(RestResponse.class)
            .assertBody(Matchers.is(responses[0]));
        req.uri().set(new URI("/second/path")).back()
            .body().set(request).back()
            .fetch()
            .as(RestResponse.class)
            .assertBody(Matchers.is(responses[1]));
    }
}
