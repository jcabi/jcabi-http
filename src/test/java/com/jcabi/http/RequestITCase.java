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

import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.response.XmlResponse;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * Integration case for {@link com.jcabi.http.request.ApacheRequest}.
 * @since 1.1
 */
@RunWith(Parameterized.class)
public final class RequestITCase {

    /**
     * Type of request.
     */
    private final transient Class<? extends Request> type;

    /**
     * Public ctor.
     * @param req Request type
     */
    public RequestITCase(final Class<? extends Request> req) {
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
        this.request(new URI("http://www.jare.io"))
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .as(XmlResponse.class)
            .assertXPath("/xhtml:html");
    }

    /**
     * BaseRequest can process not-OK response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void processesNotOkHttpResponse() throws Exception {
        this.request(new URI("http://www.jare.io/file-not-found.txt"))
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
    }

    /**
     * BaseRequest can throw a correct exception on connection error.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = IOException.class)
    public void continuesOnConnectionError() throws Exception {
        this.request(new URI("http://localhost:6868/"))
            .method(Request.GET)
            .fetch();
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
