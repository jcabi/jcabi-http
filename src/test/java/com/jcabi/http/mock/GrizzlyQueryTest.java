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
package com.jcabi.http.mock;

import com.sun.grizzly.tcp.http11.GrizzlyInputBuffer;
import com.sun.grizzly.tcp.http11.GrizzlyInputStream;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Test case for {@link GrizzlyQuery}.
 * @since 1.13
 */
public final class GrizzlyQueryTest {

    /**
     * GrizzlyQuery can return a body as a byte array.
     * @throws Exception if something goes wrong.
     */
    @Test
    public void returnsBinaryBody() throws Exception {
        final GrizzlyRequest request = Mockito.mock(GrizzlyRequest.class);
        Mockito.when(request.getRequestURI()).thenReturn("http://fake.com");
        Mockito.when(request.getHeaderNames()).thenReturn(
            Collections.emptyEnumeration()
        );
        final byte[] body = "body".getBytes();
        Mockito.when(request.getInputStream()).thenReturn(
            new GrizzlyQueryTest.MkGrizzlyInputStream(body)
        );
        MatcherAssert.assertThat(
            new GrizzlyQuery(request).binary(),
            Matchers.is(body)
        );
    }

    /**
     * Mock for GrizzlyInputStream, which returns desired byte array.
     *
     * @since 1.13
     */
    private static class MkGrizzlyInputStream extends GrizzlyInputStream {
        /**
         * Bytes to be returned by the stream.
         */
        private final transient byte[] bytes;

        /**
         * Is it empty?
         */
        private transient boolean empty;

        /**
         * Ctor.
         * @param bts Bytes
         */
        MkGrizzlyInputStream(final byte[] bts) {
            super(new GrizzlyInputBuffer());
            this.bytes = Arrays.copyOf(bts, bts.length);
        }

        @Override
        public int read(final byte[] bts) throws IOException {
            int length = -1;
            if (!this.empty) {
                System.arraycopy(this.bytes, 0, bts, 0, this.bytes.length);
                length = this.bytes.length;
                this.empty = true;
            }
            return length;
        }
    }
}
