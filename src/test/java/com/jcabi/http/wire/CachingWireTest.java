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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link CachingWire}.
 * @since 1.0
 */
final class CachingWireTest {

    /**
     * CachingWire can cache GET requests.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void cachesGetRequest() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final Request req = new JdkRequest(container.home())
            .through(CachingWire.class);
        for (int idx = 0; idx < Tv.TEN; ++idx) {
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
        container.stop();
        MatcherAssert.assertThat(container.queries(), Matchers.equalTo(1));
    }

    /**
     * CachingWire can ignore PUT requests.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void ignoresPutRequest() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .start();
        final Request req = new JdkRequest(container.home())
            .through(CachingWire.class).method(Request.PUT);
        for (int idx = 0; idx < 2; ++idx) {
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
        container.stop();
        MatcherAssert.assertThat(container.queries(), Matchers.equalTo(2));
    }

    /**
     * CachingWire can flush on regular expression match.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void flushesOnRegularExpressionMatch() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("first response"))
            .next(new MkAnswer.Simple("second response"))
            .next(new MkAnswer.Simple("third response"))
            .start();
        final Request req = new JdkRequest(container.home())
            .through(CachingWire.class, "POST /flush\\?a=1");
        req.fetch()
            .as(RestResponse.class)
            .assertBody(Matchers.containsString("first"));
        req.fetch()
            .as(RestResponse.class)
            .assertBody(Matchers.containsString("first re"));
        req.method(Request.POST).uri().path("flush")
            .queryParam("a", "1").back().fetch();
        req.fetch()
            .as(RestResponse.class)
            .assertBody(Matchers.containsString("third"));
        container.stop();
        MatcherAssert.assertThat(
            container.queries(),
            Matchers.equalTo(Tv.THREE)
        );
    }

    /**
     * CachingWire can use custom cache.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void cachesGetRequestWithCustomCache() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).next(
            new MkAnswer.Simple(HttpURLConnection.HTTP_BAD_GATEWAY)
        ).start();
        final LoadingCache<Callable<Response>, Response> cache =
            CacheBuilder
                .newBuilder()
                .build(
                    new CacheLoader<Callable<Response>, Response>() {
                        @Override
                        public Response load(final Callable<Response> query)
                            throws Exception {
                            return query.call();
                        }
                    }
                );
        final Request req = new JdkRequest(container.home())
            .through(CachingWire.class, cache);
        for (int idx = 0; idx < Tv.TEN; ++idx) {
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
        container.stop();
        MatcherAssert.assertThat(container.queries(), Matchers.equalTo(1));
    }

}
