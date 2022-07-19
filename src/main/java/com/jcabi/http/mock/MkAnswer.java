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

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.ImmutableHeader;
import com.jcabi.http.RequestBody;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.EqualsAndHashCode;

/**
 * Mock response.
 *
 * @since 0.10
 */
@Immutable
@SuppressWarnings("PMD.TooManyMethods")
public interface MkAnswer {

    /**
     * HTTP response status.
     * @return The status code
     */
    int status();

    /**
     * HTTP response headers.
     * @return The headers
     */
    Map<String, List<String>> headers();

    /**
     * HTTP response body.
     * @return The body, as a UTF-8 string
     */
    String body();

    /**
     * HTTP response body as bytes.
     * @return The body, as byte array
     */
    byte[] bodyBytes();

    /**
     * Simple implementation.
     *
     * @since 1.0
     */
    @Immutable
    @EqualsAndHashCode(of = {"code", "hdrs", "content"})
    @Loggable(Loggable.DEBUG)
    final class Simple implements MkAnswer {
        /**
         * The Charset to use.
         */
        private static final Charset CHARSET = Charset.forName("UTF-8");

        /**
         * Encapsulated response.
         */
        private final transient int code;

        /**
         * Headers.
         */
        private final transient Array<Map.Entry<String, String>> hdrs;

        /**
         * Content received.
         */
        @Immutable.Array
        private final transient byte[] content;

        /**
         * Public ctor.
         * @param body Body of HTTP response
         */
        public Simple(final String body) {
            this(HttpURLConnection.HTTP_OK, body);
        }

        /**
         * Public ctor (with empty HTTP body).
         * @param status HTTP status
         * @since 1.9
         */
        public Simple(final int status) {
            this(status, "");
        }

        /**
         * Public ctor.
         * @param status HTTP status
         * @param body Body of HTTP response
         */
        public Simple(final int status, final String body) {
            this(
                status, new Array<Map.Entry<String, String>>(),
                body.getBytes(MkAnswer.Simple.CHARSET)
            );
        }

        /**
         * Public ctor.
         * @param status HTTP status
         * @param headers HTTP headers
         * @param body Body of HTTP response
         */
        public Simple(final int status,
            final Iterable<Map.Entry<String, String>> headers,
            final byte[] body) {
            this.code = status;
            this.hdrs = new Array<>(headers);
            this.content = body.clone();
        }

        @Override
        public int status() {
            return this.code;
        }

        @Override
        @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
        public Map<String, List<String>> headers() {
            final ConcurrentMap<String, List<String>> map =
                new ConcurrentHashMap<>(0);
            for (final Map.Entry<String, String> header : this.hdrs) {
                map.putIfAbsent(header.getKey(), new LinkedList<String>());
                map.get(header.getKey()).add(header.getValue());
            }
            return map;
        }

        @Override
        public String body() {
            return new String(this.content, MkAnswer.Simple.CHARSET);
        }

        @Override
        public byte[] bodyBytes() {
            return this.content.clone();
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder(0)
                .append(this.code).append('\n');
            for (final Map.Entry<String, String> header : this.hdrs) {
                text.append(
                    Logger.format(
                        "%s: %s\n",
                        header.getKey(),
                        header.getValue()
                    )
                );
            }
            return text.append('\n')
                .append(new RequestBody.Printable(this.content))
                .toString();
        }

        /**
         * Make a copy of this answer, with an extra header.
         * @param name Name of the header
         * @param value ImmutableHeader value
         * @return New answer
         */
        public MkAnswer.Simple withHeader(final String name,
            final String value) {
            return new MkAnswer.Simple(
                this.code,
                this.hdrs.with(new ImmutableHeader(name, value)),
                this.content
            );
        }

        /**
         * Make a copy of this answer, with another status code.
         * @param status Status code
         * @return New answer
         */
        public MkAnswer.Simple withStatus(final int status) {
            return new MkAnswer.Simple(
                status,
                this.hdrs,
                this.content
            );
        }

        /**
         * Make a copy of this answer, with another body.
         * @param body Body
         * @return New answer
         */
        public MkAnswer.Simple withBody(final String body) {
            return new MkAnswer.Simple(
                this.code,
                this.hdrs,
                body.getBytes(MkAnswer.Simple.CHARSET)
            );
        }

        /**
         * Make a copy of this answer, with another body.
         * @param body Body
         * @return New answer
         */
        public MkAnswer.Simple withBody(final byte[] body) {
            return new MkAnswer.Simple(this.code, this.hdrs, body);
        }
    }

}
