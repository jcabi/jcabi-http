/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import org.glassfish.grizzly.http.server.Request;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link GrizzlyQuery}.
 * @since 1.13
 */
final class GrizzlyQueryTest {

    /**
     * GrizzlyQuery can return a body as a byte array.
     * @throws Exception if something goes wrong.
     */
    @Test
    void returnsBinaryBody() throws Exception {
        final Request request = Mockito.mock(Request.class);
        Mockito.when(request.getRequestURI()).thenReturn("http://fake.com");
        Mockito.when(request.getHeaderNames()).thenReturn(
            Collections.<String>emptyList()
        );
        final byte[] body = "body".getBytes();
        Mockito.when(request.getInputStream()).thenReturn(
            new ByteArrayInputStream(body)
        );
        MatcherAssert.assertThat(
            "should match the body",
            new GrizzlyQuery(request).binary(),
            Matchers.is(body)
        );
    }

}
