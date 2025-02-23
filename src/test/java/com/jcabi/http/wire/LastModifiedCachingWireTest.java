/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import jakarta.ws.rs.core.HttpHeaders;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsAnything;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link LastModifiedCachingWire}.
 * @since 1.15
 */
final class LastModifiedCachingWireTest {

    /**
     * Test body.
     * @todo #120:15min Clean tests shared fields and redundant variables
     *  Move constants in this file to their tests because tests must share
     *  nothing. Then also inline any redundant variables.
     *  Please also configure pdd and est in.travis.yml as done e.g. in
     *  https://github.com/jcabi/jcabi-xml/blob/master/.travis.yml
     *  For first points explanation, read:
     *  http://www.yegor256.com/2016/05/03/test-methods-must-share-nothing.html
     *  http://www.yegor256.com/2015/09/01/redundant-variables-are-evil.html
     * */
    private static final String BODY = "Test body";

    /**
     * Test body updated.
     * */
    private static final String BODY_UPDATED = "Test body updated";

    /**
     * LastModifiedCachingWire can handle requests without headers.
     * @throws Exception If fails
     */
    @Test
    void requestWithoutHeaderPassed() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK, LastModifiedCachingWireTest.BODY
                )
            ).start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class);
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(Matchers.equalTo(LastModifiedCachingWireTest.BODY));
            MatcherAssert.assertThat("should be equal 1", container.queries(), Matchers.equalTo(1));
        } finally {
            container.stop();
        }
    }

    /**
     * LastModifiedCachingWire can cache GET requests.
     * @throws Exception If fails
     */
    @Test
    void cachesGetRequest() throws Exception {
        final Map<String, String> headers = Collections.singletonMap(
            HttpHeaders.LAST_MODIFIED,
            "Wed, 15 Nov 1995 04:58:08 GMT"
        );
        final int count = 10;
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    headers.entrySet(),
                    LastModifiedCachingWireTest.BODY.getBytes()
                )
            )
            .next(
                new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_MODIFIED),
                new IsAnything<MkQuery>(),
                count
            ).start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class);
            for (int idx = 0; idx < count; ++idx) {
                req
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(LastModifiedCachingWireTest.BODY)
                    );
            }
            MatcherAssert.assertThat(
                "should be equal to count", container.queries(), Matchers.equalTo(count)
            );
        } finally {
            container.stop();
        }
    }

    /**
     * LastModifiedCachingWire can evict any previous cached entry if a new
     * response does not have a last modified header.
     * We can observe this via the If-Modified-Since headers as when the cache
     * does not contain an entry, this is not present on the request.
     * @throws Exception If fails
     */
    @Test
    void doesNotCacheGetRequestIfTheLastModifiedHeaderIsMissing()
        throws Exception {
        final String first = "Body 1";
        final String second = "Body 2";
        final String third = "Body 3";
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    Collections.singletonMap(
                        HttpHeaders.LAST_MODIFIED,
                        "Wed, 15 Nov 1995 05:58:08 GMT"
                    ).entrySet(),
                    first.getBytes()
                ),
                Matchers.not(queryContainsIfModifiedSinceHeader())
            )
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    Collections.<Map.Entry<String, String>>emptySet(),
                    second.getBytes()
                ),
                queryContainsIfModifiedSinceHeader()
            )
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    Collections.<Map.Entry<String, String>>emptySet(),
                    third.getBytes()
                ),
                Matchers.not(queryContainsIfModifiedSinceHeader())
            ).start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class);
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(Matchers.equalTo(first));
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(Matchers.equalTo(second));
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(Matchers.equalTo(third));
        } finally {
            container.stop();
        }
    }

    /**
     * LastModifiedCachingWire can resist cache eviction in the event of a non
     * OK response without a last modified header.
     * @throws Exception If fails
     * @todo #120:30min Confirm cache clearing behaviour in all non-OK responses
     *  Non-OK behaviour was not specified in #120, so for example, if the
     *  response is 404 as below, does it make any sense to keep the item in
     *  cache? Is it likely a server will respond 404, and then later the exact
     *  unmodified content is available again. I think they all need to be
     *  thought about, another dubious response might be 301 Moved Permanently,
     *  or 410 Gone etc. Or, personally I think all non-OK and OK responses
     *  should behave the same WRT to clearing the cache as the cache value is
     *  so unlikely to be returned in future.
     */
    @Test
    void doesNotEvictCacheOnNonOk()
        throws Exception {
        final String body = "Body";
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    Collections.singletonMap(
                        HttpHeaders.LAST_MODIFIED,
                        "Wed, 15 Nov 1995 06:58:08 GMT"
                    ).entrySet(),
                    body.getBytes()
                ),
                Matchers.not(queryContainsIfModifiedSinceHeader())
            )
            .next(
                new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_FOUND),
                queryContainsIfModifiedSinceHeader()
            )
            .next(
                new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_MODIFIED),
                queryContainsIfModifiedSinceHeader()
            ).start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class);
            req
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(
                    Matchers.equalTo(body)
                );
            req
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_NOT_FOUND);
            req
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(
                    Matchers.equalTo(body)
                );
        } finally {
            container.stop();
        }
    }

    /**
     * LastModifiedCachingWire cache updates with newer response.
     * @throws Exception If fails
     */
    @Test
    void cacheUpdateNewerResponse() throws Exception {
        final Map<String, String> headers = Collections.singletonMap(
            HttpHeaders.LAST_MODIFIED,
            "Wed, 16 Nov 1995 04:58:08 GMT"
        );
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    headers.entrySet(),
                    LastModifiedCachingWireTest.BODY.getBytes()
                )
            )
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_MODIFIED))
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK,
                    headers.entrySet(),
                    LastModifiedCachingWireTest.BODY_UPDATED.getBytes()
                )
            )
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_NOT_MODIFIED))
            .start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class);
            for (int idx = 0; idx < 2; ++idx) {
                req
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(LastModifiedCachingWireTest.BODY)
                    );
            }
            for (int idx = 0; idx < 2; ++idx) {
                req
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(
                            LastModifiedCachingWireTest.BODY_UPDATED
                        )
                    );
            }
            MatcherAssert.assertThat(
                "should be equal 4", container.queries(), Matchers.equalTo(2 + 2)
            );
        } finally {
            container.stop();
        }
    }

    /**
     * LastModifiedCachingWire can send a request directly
     * if it contains the "If-Modified-Since" header.
     * @throws Exception - if the test fails
     */
    @Test
    void sendsRequestDirectly() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK, LastModifiedCachingWireTest.BODY
                )
            )
            .next(
                new MkAnswer.Simple(
                    HttpURLConnection.HTTP_OK, LastModifiedCachingWireTest.BODY
                )
            )
            .start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class).header(
                    HttpHeaders.IF_MODIFIED_SINCE,
                    "Fri, 01 Jan 2016 00:00:00 GMT"
                );
            for (int idx = 0; idx < 2; ++idx) {
                req
                    .fetch()
                    .as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(LastModifiedCachingWireTest.BODY)
                    );
            }
            MatcherAssert.assertThat("should be eq 2", container.queries(), Matchers.equalTo(2));
        } finally {
            container.stop();
        }
    }

    /**
     * A Matcher that tests for the presence of the If-Modified-Since header.
     * @return The query matcher
     */
    private static Matcher<MkQuery> queryContainsIfModifiedSinceHeader() {
        return LastModifiedCachingWireTest.queryContainingHeader(
            "If-Modified-Since"
        );
    }

    /**
     * Provides a MkQuery matcher that tests if the request contains the
     * specified header.
     * @param header The header to look for
     * @return A matcher which tests for the supplied header
     */
    private static Matcher<MkQuery> queryContainingHeader(final String header) {
        return new TypeSafeMatcher<MkQuery>() {
            @Override
            protected boolean matchesSafely(final MkQuery query) {
                return query.headers().containsKey(header);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("contains ");
                description.appendText(header);
            }
        };
    }
}
