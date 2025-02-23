/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
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
        final String uri = "http://localhost:6789";
        final String method = HttpMethod.POST;
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            Assertions.assertThrows(
                IOException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new JdkRequest(new URI(uri)).method(method).fetch();
                    }
                }),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString(uri),
                    Matchers.containsString(method)
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
        final String uri = "localhost";
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            Assertions.assertThrows(
                IOException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new JdkRequest(uri).fetch();
                    }
                }),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("is incorrect"),
                    Matchers.containsString(uri)
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
        final String url = "test.com";
        final String colon = ":";
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            Assertions.assertThrows(
                MalformedURLException.class,
                new Executable() {

                    @Override
                    public void execute() throws Throwable {
                        new JdkRequest(
                            StringUtils.join(
                                url,
                                colon,
                                "80"
                            )
                        ).fetch();
                    }
                }
            ),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("unknown protocol: "),
                    Matchers.containsString(url)
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
        final String uri = "bla bla url";
        MatcherAssert.assertThat(
            "should be error with a descriptive message",
            Assertions.assertThrows(
                IllegalArgumentException.class,
                new Executable() {

                    @Override
                    public void execute() throws Throwable {
                        new JdkRequest(uri).fetch();
                    }
                }),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("Illegal character in path"),
                    Matchers.containsString(uri)
                )
            )
        );
    }
}
