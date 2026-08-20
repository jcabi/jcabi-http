/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import jakarta.ws.rs.HttpMethod;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Integration case for {@link JdkRequest}.
 * @since 1.4.1
 */
final class JdkRequestITCase {

    /**
     * Property name of Exception.
     */
    private static final String MESSAGE = "message";

    /**
     * BaseRequest throws an exception with a descriptive message showing the
     * URI and method when an error occurs.
     */
    @Test
    void throwsDescriptiveException() {
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            JdkRequestITCase.thrown(
                IOException.class,
                () -> new JdkRequest(new URI("http://localhost:6789"))
                    .method(HttpMethod.POST).fetch()
            ),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("http://localhost:6789"),
                    Matchers.containsString(HttpMethod.POST)
                )
            )
        );
    }

    /**
     * BaseRequest throws an exception with a descriptive message if there is no
     * port and no protocol mentioned in the uri.
     */
    @Test
    void failsNoProtocolNoPort() {
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            JdkRequestITCase.thrown(
                IOException.class,
                () -> new JdkRequest("localhost").fetch()
            ),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("is incorrect"),
                    Matchers.containsString("localhost")
                )
            )
        );
    }

    /**
     * BaseRequest throws an exception with a descriptive message if there is no
     * protocol mentioned in the uri.
     */
    @Test
    void failsWithPortButNoProtocol() {
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            JdkRequestITCase.thrown(
                MalformedURLException.class,
                () -> new JdkRequest(
                    StringUtils.join("test.com", ":", "80")
                ).fetch()
            ),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("unknown protocol: "),
                    Matchers.containsString("test.com")
                )
            )
        );
    }

    /**
     * BaseRequest throws an exception with a descriptive message
     * if the uri is completely wrong (e.g. bla bla1)
     */
    @Test
    void failsMalformedEntirely() {
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            JdkRequestITCase.thrown(
                IllegalArgumentException.class,
                () -> new JdkRequest("bla bla url").fetch()
            ),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("Illegal character in path"),
                    Matchers.containsString("bla bla url")
                )
            )
        );
    }

    private static Throwable thrown(final Class<? extends Throwable> type,
        final Executable exec) {
        return Assertions.assertThrows(type, exec);
    }
}
