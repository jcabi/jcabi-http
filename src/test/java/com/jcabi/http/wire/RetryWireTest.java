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
package com.jcabi.http.wire;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import javax.ws.rs.core.UriBuilder;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link RetryWire}.
 * @since 1.2
 */
public final class RetryWireTest {

    /**
     * RetryWire can make a few requests before giving up.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void makesMultipleRequests() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
            .start();
        new JdkRequest(container.home())
            .through(RetryWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
    }

    /**
     * RetryWire should strip user info when logging URL.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void stripsUserInfoWhenLogging() throws Exception {
        final StringWriter writer = new StringWriter(
        );
        Logger.getLogger(RetryWire.class)
            .addAppender(new WriterAppender(new SimpleLayout(), writer));
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
            .start();
        final URI home = container.home();
        new JdkRequest(UriBuilder.fromUri(home).userInfo("jeff:ffej").toString())
            .through(RetryWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        writer.flush();
        MatcherAssert.assertThat(
            writer.toString(),
            Matchers.containsString(
                String.format("GET %s (auth: j***j)", home)
            )
        );
        container.stop();
    }

}
