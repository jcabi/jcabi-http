/*
 * Copyright (c) 2011-2017, jcabi.com
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
package com.jcabi.http.request;

import com.jcabi.aspects.Tv;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import javax.ws.rs.HttpMethod;
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
            Assertions.assertThrows(
                MalformedURLException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new JdkRequest(uri).fetch();
                    }
                }),
            Matchers.hasProperty(
                JdkRequestITCase.MESSAGE,
                Matchers.allOf(
                    Matchers.containsString("no protocol: "),
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
            Assertions.assertThrows(
                MalformedURLException.class,
                new Executable() {

                    @Override
                    public void execute() throws Throwable {
                        new JdkRequest(
                            StringUtils.join(
                                url,
                                colon,
                                String.valueOf(Tv.EIGHTY)
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
