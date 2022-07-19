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
package com.jcabi.http.wire;

import com.jcabi.aspects.Tv;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import javax.ws.rs.core.HttpHeaders;
import org.apache.http.HttpStatus;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link AutoRedirectingWire}.
 *
 * @since 1.7
 */
public final class AutoRedirectingWireTest {

    /**
     * AutoRedirectingWire retries up to the specified number of times for
     * HTTP Status 3xx responses.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void retriesForHttpRedirectStatus() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpStatus.SC_MOVED_TEMPORARILY, "")
                // @checkstyle MultipleStringLiteralsCheck (1 line)
                .withHeader(HttpHeaders.LOCATION, "/"),
            Matchers.any(MkQuery.class),
            Integer.MAX_VALUE
        ).start();
        try {
            final int retries = Tv.THREE;
            new JdkRequest(container.home())
                .through(AutoRedirectingWire.class, retries)
                .fetch().as(RestResponse.class)
                .assertStatus(HttpStatus.SC_MOVED_TEMPORARILY);
            MatcherAssert.assertThat(
                container.takeAll(Matchers.any(MkAnswer.class)),
                Matchers.<MkQuery>iterableWithSize(retries)
            );
        } finally {
            container.stop();
        }
    }

    /**
     * AutoRedirectingWire will retry a few times and immediately return if
     * a valid response is obtained.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void returnsValidResponseAfterRetry() throws Exception {
        final String body = "success";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpStatus.SC_MOVED_TEMPORARILY, "")
                .withHeader(HttpHeaders.LOCATION, "/"),
            Matchers.any(MkQuery.class),
            2
        ).next(new MkAnswer.Simple(body)).start();
        try {
            new JdkRequest(container.home())
                .through(AutoRedirectingWire.class)
                .fetch().as(RestResponse.class)
                .assertBody(Matchers.is(body))
                .assertStatus(HttpStatus.SC_OK);
            MatcherAssert.assertThat(
                container.takeAll(Matchers.any(MkAnswer.class)),
                Matchers.<MkQuery>iterableWithSize(Tv.THREE)
            );
        } finally {
            container.stop();
        }
    }

}
