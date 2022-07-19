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
package com.jcabi.http.mock;

import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link MkAnswerMatchers}.
 * @since 1.5
 */
public final class MkAnswerMatchersTest {

    /**
     * MkAnswerMatchers should be able to match MkAnswer body.
     */
    @Test
    void canMatchBody() {
        final String body = "Hello \u20ac!";
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(body).when(query).body();
        MatcherAssert.assertThat(
            query,
            MkAnswerMatchers.hasBody(
                Matchers.is(body)
            )
        );
    }

    /**
     * MkAnswerMatchers can match MkAnswer body bytes.
     */
    @Test
    void canMatchBodyBytes() {
        final byte[] body = {0x01, 0x45, 0x21};
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(body).when(query).bodyBytes();
        MatcherAssert.assertThat(
            query,
            MkAnswerMatchers.hasBodyBytes(
                Matchers.is(body)
            )
        );
    }

    /**
     * MkAnswerMatchers should be able to match MkAnswer header.
     */
    @Test
    void canMatchHeader() {
        final String header = "Content-Type";
        final String value = "application/json";
        final MkAnswer query = Mockito.mock(MkAnswer.class);
        Mockito.doReturn(
            Collections.singletonMap(header, Collections.singletonList(value))
        ).when(query).headers();
        MatcherAssert.assertThat(
            query,
            MkAnswerMatchers.hasHeader(
                header,
                Matchers.contains(value)
            )
        );
    }
}
