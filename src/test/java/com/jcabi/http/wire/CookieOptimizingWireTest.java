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

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.net.HttpURLConnection;
import javax.ws.rs.core.HttpHeaders;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CookieOptimizingWire}.
 * @since 1.0
 */
public final class CookieOptimizingWireTest {

    /**
     * CookieOptimizingWire can transfer cookies.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void transfersCookiesOnFollow() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withHeader(HttpHeaders.SET_COOKIE, "beta=something; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "alpha=boom1; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "gamma=something; path=/")
                .withHeader(HttpHeaders.LOCATION, "/")
        ).next(new MkAnswer.Simple("")).start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, "alpha=boom5")
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers().get(HttpHeaders.COOKIE),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
            query.headers(),
            Matchers.hasEntry(
                Matchers.equalTo(HttpHeaders.COOKIE),
                Matchers.<String>everyItem(
                    Matchers.allOf(
                        Matchers.containsString("beta=something"),
                        Matchers.containsString("gamma=something"),
                        Matchers.containsString("alpha=boom1")
                    )
                )
            )
        );
    }

    /**
     * CookieOptimizingWire can avoid transferring of empty cookies.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void avoidsTransferringOfEmptyCookiesOnFollow() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
                .withHeader(HttpHeaders.SET_COOKIE, "first=A; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "second=; path=/")
                .withHeader(HttpHeaders.SET_COOKIE, "third=B; path=/")
                .withHeader(HttpHeaders.LOCATION, "/a")
        ).next(new MkAnswer.Simple("")).start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .through(CookieOptimizingWire.class)
            .header(HttpHeaders.COOKIE, "second=initial-value")
            .fetch()
            .as(RestResponse.class)
            .follow()
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        container.take();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers().get(HttpHeaders.COOKIE),
            Matchers.hasSize(1)
        );
        MatcherAssert.assertThat(
            query.headers(),
            Matchers.hasEntry(
                Matchers.equalTo(HttpHeaders.COOKIE),
                Matchers.hasItem(
                    Matchers.allOf(
                        Matchers.containsString("first=A"),
                        Matchers.containsString("third=B"),
                        Matchers.not(Matchers.containsString("second"))
                    )
                )
            )
        );
    }

}
