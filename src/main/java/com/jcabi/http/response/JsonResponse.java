/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonParsingException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

/**
 * JSON response.
 *
 * <p>This response decorator is able to parse HTTP response body as
 * a JSON document and manipulate with it afterwards, for example:
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
