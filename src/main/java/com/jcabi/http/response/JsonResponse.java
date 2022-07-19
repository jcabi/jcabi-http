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

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.stream.JsonParsingException;
import lombok.EqualsAndHashCode;

/**
 * JSON response.
 *
 * <p>This response decorator is able to parse HTTP response body as
 * a JSON document and manipulate with it afterwords, for example:
 *
 * <pre> String name = new JdkRequest("http://my.example.com")
 *   .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
 *   .fetch()
 *   .as(JsonResponse.class)
 *   .json()
 *   .readObject()
 *   .getString("name");</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class JsonResponse extends AbstractResponse {

    /**
     * Pattern matching non-ASCII characters, to escape them before parsing.
     */
    private static final Pattern CONTROL = Pattern.compile(
        "[\u0000-\u0008\u000e-\u001f\u007f-\uffff]"
    );

    /**
     * Public ctor.
     * @param resp Response
     */
    public JsonResponse(final Response resp) {
        super(resp);
    }

    /**
     * Verifies the JSON data against the element identifier argument,
     * and throws {@link AssertionError} in case of mismatch.
     * @param element Element in the JSON data of this object
     * @return This object
     */
    public JsonResponse assertJson(final String element) {
        throw new UnsupportedOperationException(
            // @checkstyle LineLength (1 line)
            "assertJson() is not implemented yet, since we are not sure which JSON query standard to use"
        );
    }

    /**
     * Read body as JSON.
     * @return Json reader
     */
    public JsonReader json() {
        final byte[] body = this.binary();
        final String json;
        json = new String(body, StandardCharsets.UTF_8);
        return new JsonResponse.VerboseReader(
            Json.createReader(
                new StringReader(
                    JsonResponse.escape(json)
                )
            ),
            json
        );
    }

    /**
     * Escape control characters in JSON parsing.
     *
     * @param input The input JSON string
     * @return Escaped JSON
     * @see <a href="http://tools.ietf.org/html/rfc4627">RFC 4627</a>
     */
    private static String escape(final CharSequence input) {
        final Matcher matcher = JsonResponse.CONTROL.matcher(input);
        final StringBuffer escaped = new StringBuffer(input.length());
        while (matcher.find()) {
            matcher.appendReplacement(
                escaped,
                String.format("\\\\u%04X", (int) matcher.group().charAt(0))
            );
        }
        matcher.appendTail(escaped);
        return escaped.toString();
    }

    /**
     * Verbose reader.
     *
     * @since 1.3.1
     */
    private static final class VerboseReader implements JsonReader {

        /**
         * Original reader.
         */
        private final transient JsonReader origin;

        /**
         * JSON body.
         */
        private final transient String json;

        /**
         * Ctor.
         * @param reader Original reader
         * @param body JSON body
         */
        VerboseReader(final JsonReader reader, final String body) {
            this.origin = reader;
            this.json = body;
        }

        @Override
        public JsonObject readObject() {
            try {
                return this.origin.readObject();
            } catch (final JsonParsingException ex) {
                throw new JsonParsingException(
                    this.json, ex, ex.getLocation()
                );
            }
        }

        @Override
        public JsonArray readArray() {
            try {
                return this.origin.readArray();
            } catch (final JsonParsingException ex) {
                throw new JsonParsingException(
                    this.json, ex, ex.getLocation()
                );
            }
        }

        @Override
        public JsonStructure read() {
            try {
                return this.origin.read();
            } catch (final JsonParsingException ex) {
                throw new JsonParsingException(
                    this.json, ex, ex.getLocation()
                );
            }
        }

        @Override
        public void close() {
            this.origin.close();
        }
    }

}
