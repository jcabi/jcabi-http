/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.stream.JsonParsingException;

/**
 * Verbose reader.
 * @since 1.3.1
 */
final class VerboseReader implements JsonReader {

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
