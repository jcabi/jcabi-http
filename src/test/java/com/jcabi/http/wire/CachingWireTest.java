/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import jakarta.ws.rs.core.HttpHeaders;
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
        for (int idx = 0; idx < 10; ++idx) {
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
        container.stop();
        MatcherAssert.assertThat("should be equal 1", container.queries(), Matchers.equalTo(1));
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
        MatcherAssert.assertThat("should be equal 1", container.queries(), Matchers.equalTo(2));
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
            "should be equal 3",
            container.queries(),
            Matchers.equalTo(3)
        );
    }

    /**
     * CachingWire bypasses the cache on every request that carries
     * a {@code Cache-Control: no-cache} directive (RFC 7234 §5.2.1.4),
     * even when an identical request with the same header has already
     * been served.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void bypassesCacheWhenCacheControlNoCache() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .start();
        final Request req = new JdkRequest(container.home())
            .through(CachingWire.class)
            .header(HttpHeaders.CACHE_CONTROL, "no-cache");
        for (int idx = 0; idx < 3; ++idx) {
            req.fetch();
        }
        container.stop();
        MatcherAssert.assertThat(
            "no-cache request must always hit origin",
            container.queries(),
            Matchers.equalTo(3)
        );
    }

    /**
     * CachingWire bypasses the cache when the request carries
     * a {@code Cache-Control: no-store} directive (RFC 7234 §5.2.1.5).
     * The response must not be stored in the cache either, so subsequent
     * identical no-store requests also reach the origin.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void bypassesCacheWhenCacheControlNoStore() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .start();
        final Request req = new JdkRequest(container.home())
            .through(CachingWire.class)
            .header(HttpHeaders.CACHE_CONTROL, "no-store");
        req.fetch();
        req.fetch();
        container.stop();
        MatcherAssert.assertThat(
            "no-store request must not be served from the cache",
            container.queries(),
            Matchers.equalTo(2)
        );
    }

    /**
     * CachingWire bypasses the cache when the request carries
     * a {@code Pragma: no-cache} directive (RFC 7234 §5.4).
     * @throws Exception If something goes wrong inside
     */
    @Test
    void bypassesCacheWhenPragmaNoCache() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .next(new MkAnswer.Simple(""))
            .start();
        final Request req = new JdkRequest(container.home())
            .through(CachingWire.class)
            .header("Pragma", "no-cache");
        req.fetch();
        req.fetch();
        container.stop();
        MatcherAssert.assertThat(
            "Pragma no-cache request must always hit origin",
            container.queries(),
            Matchers.equalTo(2)
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
        for (int idx = 0; idx < 10; ++idx) {
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        }
        container.stop();
        MatcherAssert.assertThat("should be equal 1", container.queries(), Matchers.equalTo(1));
    }

}
