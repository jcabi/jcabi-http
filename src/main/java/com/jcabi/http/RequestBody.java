/**
 * Copyright (c) 2011-2015, jcabi.com
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
package com.jcabi.http;

import com.jcabi.aspects.Immutable;
import java.util.Map;
import javax.json.JsonStructure;
import javax.validation.constraints.NotNull;

/**
 * Request body.
 *
 * <p>Instance of this interface is returned by {@link Request#body()},
 * and can be modified using one of the methods below. When modification
 * is done, method {@code back()} returns a modified instance of
 * {@link Request}, for example:
 *
 * <pre> new JdkRequest("http://my.example.com")
 *   .header("Content-Type", "application/x-www-form-urlencoded")
 *   .body()
 *   .formParam("name", "Jeff Lebowski")
 *   .formParam("age", "37")
 *   .formParam("employment", "none")
 *   .back() // returns a modified instance of Request
 *   .fetch()</pre>
 *
 * <p>Instances of this interface are immutable and thread-safe.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
public interface RequestBody {

    /**
     * Get back to the request it's related to.
     * @return The request we're in
     */
    @NotNull(message = "request is never NULL")
    Request back();

    /**
     * Get text content.
     * @return Content in UTF-8
     */
    @NotNull(message = "body can't be NULL")
    String get();

    /**
     * Set text content.
     * @param body Body content
     * @return New alternated body
     */
    @NotNull(message = "body is never NULL")
    RequestBody set(@NotNull(message = "body can't be NULL") String body);

    /**
     * Set JSON content.
     * @param json JSON object
     * @return New alternated body
     * @since 0.11
     */
    @NotNull(message = "body is never NULL")
    RequestBody set(@NotNull(message = "JSON structure can't be NULL")
        JsonStructure json);

    /**
     * Set byte array content.
     * @param body Body content
     * @return New alternated body
     */
    @NotNull(message = "modified body is never NULL")
    RequestBody set(@NotNull(message = "body can't be NULL") byte[] body);

    /**
     * Add form param.
     * @param name Query param name
     * @param value Value of the query param to set
     * @return New alternated body
     */
    @NotNull(message = "alternated body is never NULL")
    RequestBody formParam(
        @NotNull(message = "form param name can't be NULL") String name,
        @NotNull(message = "form param value can't be NULL") Object value);

    /**
     * Add form params.
     * @param params Map of params
     * @return New alternated body
     * @since 0.10
     */
    @NotNull(message = "alternated body is never NULL")
    RequestBody formParams(
        @NotNull(message = "map of params can't be NULL")
        Map<String, String> params);

    /**
     * Printer of byte array.
     */
    @Immutable
    final class Printable {
        /**
         * Byte array.
         */
        @Immutable.Array
        private final transient byte[] array;
        /**
         * Ctor.
         * @param bytes Bytes to encapsulate
         */
        public Printable(final byte[] bytes) {
            this.array = bytes;
        }
        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder(0);
            final char[] chrs = new String(
                this.array, Constants.CHARSET
            ).toCharArray();
            if (chrs.length > 0) {
                for (final char chr : chrs) {
                    // @checkstyle MagicNumber (1 line)
                    if (chr < 128) {
                        text.append(chr);
                    } else {
                        text.append("\\u").append(
                            Integer.toHexString(chr)
                        );
                    }
                }
            } else {
                text.append("<<empty>>");
            }
            return text.toString();
        }
    }

}
