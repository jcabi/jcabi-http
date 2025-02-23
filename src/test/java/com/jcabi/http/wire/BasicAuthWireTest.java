/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.mock.MkQueryMatchers;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.xml.bind.DatatypeConverter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.net.ssl.HttpsURLConnection;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test case for {@link BasicAuthWire}.
 *
 * @since 1.17.1
 */
final class BasicAuthWireTest {

    /**
     * The format of the credentials as <code>username:password</code>.
     */
    private static final String CRED_FORMAT = "%s:%s";

    /**
     * Tests if the wire generates the authorization header correctly.
     *
     * @param username The username to user for authentication
     * @param password The password to user for authentication
     * @throws Exception If something goes wrong
     */
    @ParameterizedTest
    @CsvSource({
        "Alice,  secret",
        "Bob,    s&e+c`ret",
        "user,  \u20ac\u20ac"
    })
    void testHeader(
        final String username, final String password
    ) throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        final URI uri = UriBuilder.fromUri(container.home()).userInfo(
            String.format(
                BasicAuthWireTest.CRED_FORMAT,
                URLEncoder.encode(username, StandardCharsets.UTF_8.displayName()),
                URLEncoder.encode(password, StandardCharsets.UTF_8.displayName())
            )
        ).build();
        final String expected = BasicAuthWireTest.expectHeader(
            username,
            password
        );
        new JdkRequest(uri)
            .through(BasicAuthWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();
        MatcherAssert.assertThat(
            "should be correct header",
            container.take().headers().get(HttpHeaders.AUTHORIZATION).get(0),
            Matchers.equalTo(expected)
        );
    }

    /**
     * Tests if the wire strips user info from URI, after the header was added.
     *
     * @throws Exception If something goes wrong
     */
    @Test
    void shouldStripUserInfo() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple(HttpsURLConnection.HTTP_NOT_FOUND),
            MkQueryMatchers.hasHeader(
                "Authorization", Matchers.contains(
                    BasicAuthWireTest.expectHeader("foo", "bar")
                )
            )
        ).start();
        final String userinfo = "foo:bar";
        final URI uri = UriBuilder.fromUri(container.home()).userInfo(
            userinfo
        ).build();
        MatcherAssert.assertThat(
            "should not contains user info",
            Assertions.assertThrows(
                AssertionError.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new JdkRequest(uri)
                            .through(BasicAuthWire.class)
                            .fetch()
                            .as(RestResponse.class)
                            .assertStatus(HttpURLConnection.HTTP_OK);
                    }
                }
            ),
            Matchers.<AssertionError>hasToString(
                Matchers.not(
                    Matchers.containsString(userinfo)
                )
            )
        );
        container.stop();
    }

    /**
     * Creates the expected authorization header value for the
     * given username.
     *
     * @param username The username to create the header for
     * @param password The password to create the header for
     * @return The header value in the form
     *  <code>Basic &lt;base64 of username:password&gt;</code>
     */
    private static String expectHeader(
        final String username,
        final String password
    ) {
        final String credentials = DatatypeConverter.printBase64Binary(
            String.format(
                BasicAuthWireTest.CRED_FORMAT,
                username,
                password
            ).getBytes(StandardCharsets.UTF_8)
        );
        return String.format("Basic %s", credentials);
    }
}
