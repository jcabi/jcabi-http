/*
 * Copyright (c) 2011-2017, jcabi.com
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

import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Integration case for {@link com.jcabi.http.request.ApacheRequest}.
 * @since 1.1
 */
class RequestITCase extends RequestTestTemplate {

    /**
     * BaseRequest can fetch HTTP request and process HTTP response.
     * @throws Exception If something goes wrong inside
     * @param type Request type
     */
    @Values
    @ParameterizedTest
    void sendsHttpRequestAndProcessesHttpResponse(
        final Class<? extends Request> type
    ) throws Exception {
        RequestTestTemplate.request(new URI("http://www.jare.io"), type)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/xhtml:html");
    }

    /**
     * BaseRequest can process not-OK response.
     * @throws Exception If something goes wrong inside
     * @param type Request type
     */
    @Values
    @ParameterizedTest
    void processesNotOkHttpResponse(
        final Class<? extends Request> type
    ) throws Exception {
        RequestTestTemplate.request(new URI("http://www.jare.io/file-not-found.txt"), type)
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

}
