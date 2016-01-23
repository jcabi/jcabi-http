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
package com.jcabi.http.wire;

import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQuery;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsAnything;
import org.junit.Test;

/**
 * Test case for {@link LastModifiedCachingWire}.
 * @author Igor Piddubnyi (igor.piddubnyi@gmail.com)
 * @version $Id$
 * @since 1.15
 */
public final class LastModifiedCachingWireTest {

    /**
     * Test body.
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
    public void requestWithoutHeaderPassed() throws Exception {
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
            MatcherAssert.assertThat(container.queries(), Matchers.equalTo(1));
        } finally {
            container.stop();
        }
    }

    /**
     * LastModifiedCachingWire can cache GET requests.
     * @throws Exception If fails
     */
    @Test
    public void cachesGetRequest() throws Exception {
        final Map<String, String> headers = Collections.singletonMap(
            HttpHeaders.LAST_MODIFIED,
            "Wed, 15 Nov 1995 04:58:08 GMT"
        );
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
                Tv.TEN
            ).start();
        try {
            final Request req = new JdkRequest(container.home())
                .through(LastModifiedCachingWire.class);
            for (int idx = 0; idx < Tv.TEN; ++idx) {
                req.fetch().as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(LastModifiedCachingWireTest.BODY)
                );
            }
            MatcherAssert.assertThat(
                container.queries(), Matchers.equalTo(Tv.TEN)
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
    public void cacheUpdateNewerResponse() throws Exception {
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
                req.fetch().as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(LastModifiedCachingWireTest.BODY)
                );
            }
            for (int idx = 0; idx < 2; ++idx) {
                req.fetch().as(RestResponse.class)
                    .assertStatus(HttpURLConnection.HTTP_OK)
                    .assertBody(
                        Matchers.equalTo(
                            LastModifiedCachingWireTest.BODY_UPDATED
                        )
                );
            }
            MatcherAssert.assertThat(
                container.queries(), Matchers.equalTo(2 + 2)
            );
        } finally {
            container.stop();
        }
    }
}
