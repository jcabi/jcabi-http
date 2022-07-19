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
package com.jcabi.http.request;

import com.jcabi.http.Request;
import com.jcabi.http.response.RestResponse;
import java.io.ByteArrayInputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test case for {@link FakeRequest}.
 * @since 1.0
 */
final class FakeRequestTest {

    /**
     * FakeRequest can fetch a fake response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void sendsHttpRequestAndProcessesHttpResponse() throws Exception {
        this.generateMainRequest()
            .withBody("how are you?")
            .uri().path("/helloall").back()
            .method(Request.POST)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .assertBody(Matchers.containsString("are you?"));
    }

    /**
     * FakeRequest can fetch a fake response with binary response.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    void sendsHttpRequestAndProcessesHttpBinaryResponse()
        throws Exception {
        final byte[] content = "Binary body content".getBytes();
        this.generateMainRequest()
            .withBody(content)
            .uri().path("/binContent").back()
            .method(Request.POST)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
            .assertBinary(Matchers.equalTo(content));
    }

    /**
     * FakeRequest can change URI.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void changesUri() throws Exception {
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
    void changesUriInResponse() throws Exception {
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
     */
    @Test
    void fetchThrowsExceptionWhenBodyIsNotEmpty() {
        Assertions.assertThrows(
            IllegalStateException.class,
            new Executable() {
                @Override
                public void execute() throws Throwable {
                    new FakeRequest()
                        .withStatus(HttpURLConnection.HTTP_OK)
                        .withBody("blah")
                        .fetch(
                            new ByteArrayInputStream(
                                "foo".getBytes(StandardCharsets.UTF_8)
                            )
                        );
                }
            }
        );
    }

    /**
     * FakeRequest returns the Response Body if the Request Body is set.
     * @throws Exception If something goes wrong inside.
     * @link https://github.com/jcabi/jcabi-http/issues/47
     */
    @Test
    void fakeRequestReturnsResponseBody() throws Exception {
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
     */
    @Test
    void identifiesUniquely() {
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
     * Helper method that generates a FakeRequest.
     * @return An instance of FakeRequest.
     */
    private FakeRequest generateMainRequest() {
        return new FakeRequest()
            .withStatus(HttpURLConnection.HTTP_OK)
            .withReason("OK")
            .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
    }

}
