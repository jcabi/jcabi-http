/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
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
        final Response resp = new FakeRequest()
            .withBody("{\n\t\r\"foo-foo\":2,\n\"bar\":\"\u20ac\"}")
            .fetch();
        final JsonResponse response = new JsonResponse(resp);
        MatcherAssert.assertThat(
            "should be equal 2",
            response.json().readObject().getInt("foo-foo"),
            Matchers.equalTo(2)
        );
        MatcherAssert.assertThat(
            "should be equal \u20ac",
            response.json().readObject().getString("bar"),
            Matchers.equalTo("\u20ac")
        );
    }

    /**
     * JsonResponse can read control characters.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void readsControlCharacters() throws Exception {
        final Response resp = new FakeRequest()
            .withBody("{\"test\":\n\"\u001Fblah\uFFFDcwhoa\u0000!\"}").fetch();
        final JsonResponse response = new JsonResponse(resp);
        MatcherAssert.assertThat(
            "should be \u001Fblah\uFFFDcwhoa\u0000!",
            response.json().readObject().getString("test"),
            Matchers.is("\u001Fblah\uFFFDcwhoa\u0000!")
        );
    }

    /**
     * JsonResponse logs the JSON body for JSON object parse errors.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsForInvalidJsonObject() throws Exception {
        final String body = "{\"test\": \"logged!\"$@%#^&%@$#}";
        final Response resp = new FakeRequest().withBody(body).fetch();
        MatcherAssert.assertThat(
            "should contains json body",
            Assertions.assertThrows(
                JsonParsingException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new JsonResponse(resp).json().readObject();
                    }
                },
                "readObject() should have thrown JsonParsingException"
            ),
            Matchers.hasToString(Matchers.containsString(body))
        );
    }

    /**
     * JsonResponse logs the JSON body for JSON array parse errors.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsForInvalidJsonArray() throws Exception {
        final String body = "[\"test\": \"logged!\"$@%#^&%@$#]";
        final Response resp = new FakeRequest().withBody(body).fetch();
        MatcherAssert.assertThat(
            "should contains json body",
            Assertions.assertThrows(
                JsonParsingException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new JsonResponse(resp).json().readArray();
                    }
                },
                "readArray() should have thrown JsonParsingException"
            ),
            Matchers.hasToString(
                Matchers.containsString(
                    body
                )
            )
        );
    }

    /**
     * JsonResponse logs the JSON body for JSON read() parse errors.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    void logsForInvalidJson() throws Exception {
        final String body = "{test:[]}}}";
        final Response resp = new FakeRequest().withBody(body).fetch();
        MatcherAssert.assertThat(
            "should contains json body",
            Assertions.assertThrows(
                JsonParsingException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        new JsonResponse(resp).json().read();
                    }
                },
                "readStructure() should have thrown JsonParsingException"
            ),
            Matchers.<JsonParsingException>hasToString(
                Matchers.containsString(
                    body
                )
            )
        );
    }

}
