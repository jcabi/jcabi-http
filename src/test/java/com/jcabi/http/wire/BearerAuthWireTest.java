/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.DefaultResponse;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.immutable.Array;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link BearerAuthWire}.
 *
 * @since 2.0
 */
final class BearerAuthWireTest {

    @Test
    void bearerAuthWireWorks() throws IOException {
        MatcherAssert.assertThat(
            "should be correct header",
            BearerAuthWireTest.authorization("my-bearer-token"),
            Matchers.hasItem("Bearer my-bearer-token")
        );
    }

    @Test
    void onlyOneBearerAuthWireWorks() throws IOException {
        MatcherAssert.assertThat(
            "there should be no more than one 'Authorization' header",
            BearerAuthWireTest.authorization(
                "my-third-bearer-token",
                "my-second-bearer-token",
                "my-first-bearer-token"
            ).size(),
            Matchers.equalTo(1)
        );
    }

    @Test
    void onlyTheFirstBearerAuthWireWorks() throws IOException {
        MatcherAssert.assertThat(
            "should be correct header",
            BearerAuthWireTest.authorization(
                "my-third-bearer-token",
                "my-second-bearer-token",
                "my-first-bearer-token"
            ).get(0),
            Matchers.equalTo("Bearer my-first-bearer-token")
        );
    }

    @Test
    void keepsAuthorizationHeaderProvidedByRequest() throws IOException {
        MatcherAssert.assertThat(
            "should keep only the header provided by the request",
            BearerAuthWireTest.provided(HttpHeaders.AUTHORIZATION),
            Matchers.contains("Basic dXNlcjpwYXNz")
        );
    }

    @Test
    void keepsLowercaseAuthorizationHeaderProvidedByRequest()
        throws IOException {
        MatcherAssert.assertThat(
            "should not add a second header next to the lowercase one",
            BearerAuthWireTest.provided("authorization"),
            Matchers.contains("Basic dXNlcjpwYXNz")
        );
    }

    @Test
    void keepsMixedCaseAuthorizationHeaderProvidedByRequest()
        throws IOException {
        MatcherAssert.assertThat(
            "should not add a second header next to the mixed case one",
            BearerAuthWireTest.provided("aUtHoRiZaTiOn"),
            Matchers.contains("Basic dXNlcjpwYXNz")
        );
    }

    @Test
    void keepsLowercaseAuthorizationEntryUntouchedForWrappedWire()
        throws IOException {
        MatcherAssert.assertThat(
            "should forward exactly one entry with the original value",
            BearerAuthWireTest.forwarded(
                "authorization", "Custom d15t1nct-v4lue/+=~ 0x7f"
            ),
            Matchers.contains("Custom d15t1nct-v4lue/+=~ 0x7f")
        );
    }

    @Test
    void keepsMixedCaseAuthorizationEntryUntouchedForWrappedWire()
        throws IOException {
        MatcherAssert.assertThat(
            "should forward exactly one entry with the original value",
            BearerAuthWireTest.forwarded(
                "aUtHoRiZaTiOn", "Custom 0r1g1n4l-v4lue/+=~ 0x3c"
            ),
            Matchers.contains("Custom 0r1g1n4l-v4lue/+=~ 0x3c")
        );
    }

    private static List<String> forwarded(final String name, final String value)
        throws IOException {
        final List<Map.Entry<String, String>> seen = new ArrayList<>(1);
        new BearerAuthWire(new BearerAuthWireTest.Spy(seen), "configured-bearer-token").send(
            new JdkRequest("http://localhost"),
            "http://localhost",
            Request.GET,
            Collections.singletonList(
                new AbstractMap.SimpleImmutableEntry<>(name, value)
            ),
            new ByteArrayInputStream(new byte[0]),
            0,
            0
        );
        return seen.stream()
            .filter(e -> e.getKey().equalsIgnoreCase(HttpHeaders.AUTHORIZATION))
            .map(Map.Entry::getValue)
            .collect(Collectors.toList());
    }

    private static List<String> provided(final String name) throws IOException {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        try {
            new JdkRequest(UriBuilder.fromUri(container.home()).build())
                .header(name, "Basic dXNlcjpwYXNz")
                .through(BearerAuthWire.class, "ignored-bearer-token")
                .fetch();
            return container.take().headers().entrySet().stream().filter(
                e -> e.getKey().equalsIgnoreCase(HttpHeaders.AUTHORIZATION)
            )
            .flatMap(e -> e.getValue().stream())
            .collect(Collectors.toList());
        } finally {
            container.stop();
        }
    }

    private static List<String> authorization(final String... tokens)
        throws IOException {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        try {
            Request request = new JdkRequest(
                UriBuilder.fromUri(container.home()).build()
            );
            for (final String token : tokens) {
                request = request.through(BearerAuthWire.class, token);
            }
            request.fetch();
            return container.take().headers().get(HttpHeaders.AUTHORIZATION);
        } finally {
            container.stop();
        }
    }

    /**
     * Wire that records the headers it receives.
     *
     * @since 2.0
     */
    private static final class Spy implements Wire {

        /**
         * Headers received.
         */
        private final transient List<Map.Entry<String, String>> seen;

        Spy(final List<Map.Entry<String, String>> seen) {
            this.seen = seen;
        }

        @Override
        public Response send(
            final Request req,
            final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final InputStream content,
            final int connect,
            final int read
        ) {
            this.seen.addAll(headers);
            return new DefaultResponse(
                req, HttpURLConnection.HTTP_OK, "OK", new Array<>(), new byte[0]
            );
        }
    }
}
