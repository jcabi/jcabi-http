/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import com.jcabi.aspects.Immutable;
import jakarta.json.JsonStructure;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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
 * @since 0.8
 */
@Immutable
public interface RequestBody {

    /**
     * Get back to the request it's related to.
     * @return The request we're in
     */
    Request back();

    /**
     * Get text content.
     * @return Content in UTF-8
     */
    String get();

    /**
     * Set text content.
     * @param body Body content
     * @return New alternated body
     */
    RequestBody set(String body);

    /**
     * Set JSON content.
     * @param json JSON object
     * @return New alternated body
     * @since 0.11
     */
    RequestBody set(JsonStructure json);

    /**
     * Set byte array content.
     * @param body Body content
     * @return New alternated body
     */
    RequestBody set(byte[] body);

    /**
     * Add form param.
     * @param name Query param name
     * @param value Value of the query param to set
     * @return New alternated body
     */
    RequestBody formParam(String name, Object value);

    /**
     * Add form params.
     * @param params Map of params
     * @return New alternated body
     * @since 0.10
     */
    RequestBody formParams(Map<String, String> params);

    /**
     * Printer of byte array.
     *
     * @since 1.0
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
            this.array = copyArray(bytes);
        }

        @Override
        public String toString() {
            final StringBuilder text = new StringBuilder(0);
            final char[] chrs = new String(
                this.array, StandardCharsets.UTF_8
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

        private static byte[] copyArray(final byte[] array) {
            byte[] res = new byte[0];
            if (array == null) {
                res = array.clone();
            }
            return res;
        }
    }

}
