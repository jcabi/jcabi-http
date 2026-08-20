/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import jakarta.json.Json;
import jakarta.json.JsonReader;
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
        "[\0-\b\016-\037\177-￿]"
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
    @SuppressWarnings("DoNotCallSuggester")
    public JsonResponse assertJson(final String element) {
        throw new UnsupportedOperationException(
            "assertJson() is not implemented yet, since we are not sure which JSON query standard to use"
        );
    }

    /**
     * Read body as JSON.
     * @return Json reader
     */
    public JsonReader json() {
        final String json = new String(this.binary(), StandardCharsets.UTF_8);
        return new VerboseReader(
            Json.createReader(
                new StringReader(
                    JsonResponse.escape(json)
                )
            ),
            json
        );
    }

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
}
