/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import java.util.ArrayList;
import java.util.Collection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link FcWire}.
 * @since 1.0
 */
final class FcWireTest {

    /**
     * FileCachingWire can cache GET requests.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void cachesGetRequest() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final Request req = new JdkRequest(container.home())
            .through(FcWire.class);
        for (int idx = 0; idx < 10; ++idx) {
            req.fetch();
        }
        container.stop();
        MatcherAssert.assertThat(
            "should be equal 1",
            container.queries(),
            Matchers.equalTo(1)
        );
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
            .through(FcWire.class).method(Request.PUT);
        for (int idx = 0; idx < 2; ++idx) {
            req.fetch();
        }
        container.stop();
        MatcherAssert.assertThat(
            "should be equal 2",
            container.queries(),
            Matchers.equalTo(2)
        );
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
            .through(FcWire.class, "POST /flush\\?a=1");
        final Collection<String> bodies = new ArrayList<>(3);
        bodies.add(req.fetch().body());
        bodies.add(req.fetch().body());
        req.method(Request.POST).uri().path("flush")
            .queryParam("a", "1").back().fetch();
        bodies.add(req.fetch().body());
        container.stop();
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should cache until it gets flushed",
                bodies,
                Matchers.contains(
                    "first response", "first response", "third response"
                )
            ),
            () -> MatcherAssert.assertThat(
                "should be equal 3",
                container.queries(),
                Matchers.equalTo(3)
            )
        );
    }

}
