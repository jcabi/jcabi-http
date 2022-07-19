/*
 * Copyright (c) 2011-2022, jcabi.com
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
package com.jcabi.http.response;

import com.jcabi.http.request.JdkRequest;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Integration test for {@link RestResponse}.
 *
 * @since 1.17.5
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals") final class RestResponseITCase {
    @Test
    void readsCookiesSeveralValues() throws IOException {
        final RestResponse resp = new JdkRequest(
            "https://httpbin.org/cookies/set?ijk=efg&xyz=abc"
        )
            .fetch()
            .as(RestResponse.class);
        Assertions.assertAll(
            new Executable() {
                @Override
                public void execute() {
                    MatcherAssert.assertThat(
                        resp.cookie("ijk"),
                        Matchers.hasProperty("value", Matchers.is("efg"))
                    );
                }
            },
            new Executable() {
                @Override
                public void execute() {
                    MatcherAssert.assertThat(
                        resp.cookie("xyz"),
                        Matchers.hasProperty("value", Matchers.is("abc"))
                    );
                }
            }
        );
    }

    @Test
    void readsCookies() throws IOException {
        MatcherAssert.assertThat(
            new JdkRequest("https://httpbin.org/cookies/set?foo=bar")
                .fetch()
                .as(RestResponse.class)
                .cookie("foo"),
            Matchers.hasProperty("value", Matchers.is("bar"))
        );
    }
}
