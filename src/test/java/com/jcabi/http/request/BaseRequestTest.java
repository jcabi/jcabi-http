/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.http.Request;
import com.jcabi.http.Wire;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.immutable.ArrayMap;
import jakarta.json.Json;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
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
            "should has the right destination URI",
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
            "should equals to '{\"foo\":\"test 1\"}'",
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
            "should has 'http://localhost:8080/'",
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
            "should not equals",
            new BaseRequest(wire, "").header("header-1", "value-1"),
            Matchers.not(
                Matchers.equalTo(
                    new BaseRequest(wire, "").header("header-2", "value-2")
                )
            )
        );
        MatcherAssert.assertThat(
            "should equals",
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
            "should be error when multipart-body without content-type",
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
            "should be error when multipart-body without content-type",
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
            "should be match to 'foo1=bar1&foo2=bar2&foo3=bar3&foo4=bar4'",
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
