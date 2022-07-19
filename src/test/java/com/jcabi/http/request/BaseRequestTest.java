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
package com.jcabi.http.request;

import com.jcabi.http.Request;
import com.jcabi.http.Wire;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.immutable.ArrayMap;
import java.io.IOException;
import javax.json.Json;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

/**
 * Test case for {@link BaseRequest}.
 *
 * @since 1.0
 */
final class BaseRequestTest {

    /**
     * Property name of Exception.
     */
    private static final String MESSAGE = "message";

    /**
     * BaseRequest can build the right destination URI.
     */
    @Test
    void buildsDestinationUri() {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            new BaseRequest(wire, "http://localhost:88/t/f")
                .uri().path("/bar").queryParam("u1", "\u20ac")
                .queryParams(new ArrayMap<String, String>().with("u2", ""))
                .userInfo("hey:\u20ac").back().uri().get(),
            Matchers.hasToString(
                "http://hey:%E2%82%AC@localhost:88/t/f/bar?u1=%E2%82%AC&u2="
            )
        );
    }

    /**
     * BaseRequest can set body to JSON.
     */
    @Test
    void printsJsonInBody() {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            new BaseRequest(wire, "http://localhost:88/x").body().set(
                Json.createObjectBuilder().add("foo", "test 1").build()
            ).get(),
            Matchers.equalTo("{\"foo\":\"test 1\"}")
        );
    }

    /**
     * BaseRequest can include the port number.
     */
    @Test
    void includesPort() {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            // @checkstyle MagicNumber (2 lines)
            new BaseRequest(wire, "http://localhost")
                .uri().port(8080).back().uri().get(),
            Matchers.hasToString("http://localhost:8080/")
        );
    }

    /**
     * FakeRequest can identify itself uniquely.
     */
    @Test
    void identifiesUniquely() {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            new BaseRequest(wire, "").header("header-1", "value-1"),
            Matchers.not(
                Matchers.equalTo(
                    new BaseRequest(wire, "").header("header-2", "value-2")
                )
            )
        );
        MatcherAssert.assertThat(
            new BaseRequest(wire, ""),
            Matchers.equalTo(new BaseRequest(wire, ""))
        );
    }

    /**
     * Throws exception when using formParam on multipart-body without
     * content-type defined.
     */
    @Test
    void exceptionWhenMissingContentType() {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IllegalStateException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new BaseRequest(wire, "")
                            .multipartBody()
                            .formParam("a", "value")
                            .back();
                    }
                }
            ),
            Matchers.hasProperty(
                BaseRequestTest.MESSAGE,
                Matchers.is(BaseRequestTest.boundaryErrorMesg())
            )
        );
    }

    /**
     * Throws exception when using formParam on multipartbody without boundary
     * provided in content-type defined.
     */
    @Test
    void exceptionWhenMissingBoundary() {
        final Wire wire = Mockito.mock(Wire.class);
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IllegalStateException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new BaseRequest(wire, "")
                            .header(
                                HttpHeaders.CONTENT_TYPE,
                                MediaType.MULTIPART_FORM_DATA
                            )
                            .multipartBody().formParam("b", "val").back();
                    }
                }
            ),
            Matchers.hasProperty(
                BaseRequestTest.MESSAGE,
                Matchers.is(BaseRequestTest.boundaryErrorMesg())
            )
        );
    }

    @Test
    void shouldHaveCorrectFormParameters() throws IOException {
        final MkContainer srv = new MkGrizzlyContainer()
            .next(new MkAnswer.Simple("OK")).start();
        new JdkRequest(srv.home())
            .body()
            .formParam("foo1", "bar1")
            .back()
            .body()
            .formParam("foo2", "bar2")
            .back()
            .body()
            .formParam("foo3", "bar3")
            .formParam("foo4", "bar4")
            .back()
            .method(Request.POST)
            .fetch();
        MatcherAssert.assertThat(
            srv.take().body(),
            Matchers.is("foo1=bar1&foo2=bar2&foo3=bar3&foo4=bar4")
        );
    }

    /**
     * Boundary error message.
     *
     * @return Message error as String.
     */
    private static String boundaryErrorMesg() {
        return "Content-Type: multipart/form-data requires boundary";
    }
}
