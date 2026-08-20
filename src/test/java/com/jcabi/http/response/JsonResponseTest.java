/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.Response;
import com.jcabi.http.request.FakeRequest;
import jakarta.json.stream.JsonParsingException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test case for {@link JsonResponse}.
 * @since 1.1
 */
final class JsonResponseTest {

    /**
     * JsonResponse can read and return a JSON document.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void readsJsonDocument() throws Exception {
        final JsonResponse response = new JsonResponse(
            new FakeRequest().withBody(
                String.format("{%n\t\"foo-foo\":2,%n\"bar\":\"€\"}")
            ).fetch()
        );
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should be equal 2",
                response.json().readObject().getInt("foo-foo"),
                Matchers.equalTo(2)
            ),
            () -> MatcherAssert.assertThat(
                "should be equal €",
                response.json().readObject().getString("bar"),
                Matchers.equalTo("€")
            )
        );
    }

    /**
     * JsonResponse can read control characters.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void readsControlCharacters() throws Exception {
        MatcherAssert.assertThat(
            "should be \037blah�cwhoa\0!",
            new JsonResponse(
                new FakeRequest().withBody(
                    String.format("{\"test\":%n\"\037blah�cwhoa\0!\"}")
                ).fetch()
            ).json().readObject().getString("test"),
            Matchers.is("\037blah�cwhoa\0!")
        );
    }

    /**
     * JsonResponse logs the JSON body for JSON object parse errors.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsForInvalidJsonObject() throws Exception {
        final String body = "{\"test\": \"logged!\"$@%#^&%@$#}";
        final Response resp = new FakeRequest().withBody(body).fetch();
        MatcherAssert.assertThat(
            "readObject() should complain about the json body",
            JsonResponseTest.thrown(
                JsonParsingException.class,
                () -> new JsonResponse(resp).json().readObject()
            ),
            Matchers.hasToString(Matchers.containsString(body))
        );
    }

    /**
     * JsonResponse logs the JSON body for JSON array parse errors.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsForInvalidJsonArray() throws Exception {
        final String body = "[\"test\": \"logged!\"$@%#^&%@$#]";
        final Response resp = new FakeRequest().withBody(body).fetch();
        MatcherAssert.assertThat(
            "readArray() should complain about the json body",
            JsonResponseTest.thrown(
                JsonParsingException.class,
                () -> new JsonResponse(resp).json().readArray()
            ),
            Matchers.hasToString(Matchers.containsString(body))
        );
    }

    /**
     * JsonResponse logs the JSON body for JSON read() parse errors.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsForInvalidJson() throws Exception {
        final String body = "{test:[]}}}";
        final Response resp = new FakeRequest().withBody(body).fetch();
        MatcherAssert.assertThat(
            "read() should complain about the json body",
            JsonResponseTest.thrown(
                JsonParsingException.class,
                () -> new JsonResponse(resp).json().read()
            ),
            Matchers.hasToString(Matchers.containsString(body))
        );
    }

    private static Throwable thrown(final Class<? extends Throwable> type,
        final Executable exec) {
        return Assertions.assertThrows(type, exec);
    }
}
