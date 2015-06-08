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
package com.jcabi.http.mock;

import com.jcabi.http.JcabiHttp;
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import com.jcabi.http.wire.VerboseWire;
import java.net.HttpURLConnection;
import java.util.NoSuchElementException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.IsAnything;
import org.junit.Test;

/**
 * Test case for {@link MkContainer}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class MkContainerTest {

    /**
     * MkContainer can return required answers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void worksAsServletContainer() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK, "works fine!"))
            .start();
        new JdkRequest(container.home())
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(Matchers.startsWith("works"));
        container.stop();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.method(),
            Matchers.equalTo(JcabiHttp.GET)
        );
    }

    /**
     * MkContainer can understand duplicate headers.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void understandsDuplicateHeaders() throws Exception {
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(""))
            .start();
        final String header = "X-Something";
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .header(header, MediaType.TEXT_HTML)
            .header(header, MediaType.TEXT_XML)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        final MkQuery query = container.take();
        MatcherAssert.assertThat(
            query.headers().get(header),
            Matchers.hasSize(2)
        );
    }

    /**
     * MkContainer can return certain answers for matching conditions.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void answersConditionally() throws Exception {
        final String match = "matching";
        final String mismatch = "not matching";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(mismatch),
            Matchers.not(new IsAnything<MkQuery>())
        ).next(new MkAnswer.Simple(match), new IsAnything<MkQuery>()).start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(
                Matchers.allOf(
                    Matchers.is(match),
                    Matchers.not(mismatch)
                )
            );
        container.stop();
    }

    /**
     * MkContainer returns HTTP 500 if no answers match.
     * @throws Exception If something goes wrong inside
     */
    @Test(expected = NoSuchElementException.class)
    public void returnsErrorIfNoMatches() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("not supposed to match"),
            Matchers.not(new IsAnything<MkQuery>())
        ).start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        container.stop();
        container.take();
    }

    /**
     * MkContainer can answer multiple times for matching requests.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void canAnswerMultipleTimes() throws Exception {
        final String body = "multiple";
        final int times = 5;
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(body),
            new IsAnything<MkQuery>(),
            times
        ).start();
        final Request req = new JdkRequest(container.home())
            .through(VerboseWire.class);
        for (int idx = 0; idx < times; idx += 1) {
            req.fetch().as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK)
                .assertBody(Matchers.is(body));
        }
        container.stop();
    }

    /**
     * MkContainer can prioritize multiple matching answers by using the
     * first matching request.
     * @throws Exception If something goes wrong inside.
     */
    @Test
    public void prioritizesMatchingAnswers() throws Exception {
        final String first = "first";
        final String second = "second";
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(first), new IsAnything<MkQuery>())
            .next(new MkAnswer.Simple(second), new IsAnything<MkQuery>())
            .start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .assertBody(
                Matchers.allOf(
                    Matchers.is(first),
                    Matchers.not(second)
                )
            );
        container.stop();
    }

    /**
     * MkContainer can return the query that matched a certain response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void takesMatchingQuery() throws Exception {
        final String request = "reqBodyMatches";
        final String response = "respBodyMatches";
        final MkContainer container = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple(response))
            .next(new MkAnswer.Simple("bleh"))
            .start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .method(HttpMethod.POST)
            .body().set(request).back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .method(HttpMethod.POST)
            .body().set("reqBodyMismatches").back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        MatcherAssert.assertThat(
            container.take(MkAnswerMatchers.hasBody(Matchers.is(response))),
            MkQueryMatchers.hasBody(Matchers.is(request))
        );
        container.stop();
    }

    /**
     * MkContainer can return all queries that matched a certain response.
     * @throws Exception If something goes wrong inside
     */
    @Test
    @SuppressWarnings("unchecked")
    public void takesAllMatchingQueries() throws Exception {
        final String match = "multipleRequestMatches";
        final String mismatch = "multipleRequestNotMatching";
        final String response = "multipleResponseMatches";
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(response),
            MkQueryMatchers.hasBody(Matchers.is(match)),
            2
        ).next(new MkAnswer.Simple("blaa")).start();
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .method(HttpMethod.POST)
            .body().set(match).back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK)
            .back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        new JdkRequest(container.home())
            .through(VerboseWire.class)
            .method(HttpMethod.POST)
            .body().set(mismatch).back()
            .fetch().as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        MatcherAssert.assertThat(
            container.takeAll(MkAnswerMatchers.hasBody(Matchers.is(response))),
            Matchers.allOf(
                Matchers.<MkQuery>iterableWithSize(2),
                Matchers.hasItems(
                    MkQueryMatchers.hasBody(Matchers.is(match))
                ),
                Matchers.not(
                    Matchers.hasItems(
                        MkQueryMatchers.hasBody(Matchers.is(mismatch))
                    )
                )
            )
        );
        container.stop();
    }
}
