/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import java.io.IOException;
import java.util.Arrays;
import lombok.EqualsAndHashCode;

/**
 * A JSON response provided by the Jackson Project.
 *
 * @since 1.17
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class JacksonResponse extends AbstractResponse {
    /**
     * Ctor.
     *
     * @param resp Response
     */
    public JacksonResponse(final Response resp) {
        super(resp);
    }

    /**
     * Read the body as JSON.
     *
     * @return JSON reader.
     */
    public JsonReader json() {
        return new JsonReader(
            this.binary()
        );
    }

    /**
     * A tree representation views of JSON documents.
     *
     * @since 1.17.1
     */
    public static final class JsonReader {
        /**
         * Jackson's ObjectMapper. Allow unquoted control characters when
         * parsing by default.
         */
        private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature());

        /**
         * Response body.
         */
        private final transient byte[] body;

        /**
         * Public constructor.
         *
         * @param bytes The HTTP response body as an array of bytes.
         */
        public JsonReader(final byte[] bytes) {
            this.body = Arrays.copyOf(bytes, bytes.length);
        }

        /**
         * Returns a mutable JSON array node, if the parsed JSON is a valid
         * array.
         *
         * @return JSON array node.
         * @throws IOException If the body is not a valid JSON or JSON array.
         */
        public ArrayNode readArray() throws IOException {
            final JsonNode node = this.read();
            if (!node.isArray()) {
                throw new IOException(
                    "Cannot read as an array. The JSON is not a valid array."
                );
            }
            return (ArrayNode) node;
        }

        /**
         * Returns a mutable JSON object node, if the parsed JSON is a valid
         * object.
         *
         * @return JSON object node.
         * @throws IOException If the body is not a valid JSON or JSON object.
         */
        public ObjectNode readObject() throws IOException {
            final JsonNode node = this.read();
            if (!node.isObject()) {
                throw new IOException(
                    "Cannot read as an object. The JSON is not a valid object."
                );
            }
            return (ObjectNode) node;
        }

        /**
         * Returns an immutable JSON node.
         *
         * @return JSON node.
         * @throws IOException If the body is not a valid JSON.
         */
        public JsonNode read() throws IOException {
            return MAPPER.readTree(this.body);
        }
    }
}
