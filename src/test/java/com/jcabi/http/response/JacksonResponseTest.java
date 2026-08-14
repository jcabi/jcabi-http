/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.jcabi.http.request.FakeRequest;
import java.io.IOException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test case for {@link JacksonResponse}.
 *
 * @since 1.17
 */
final class JacksonResponseTest {
    /**
     * JacksonResponse can read and return a JSON document.
     *
     * @throws IOException If anything goes wrong when parsing.
     */
    @Test
    void canReadJsonDocument() throws IOException {
        final JacksonResponse response = new FakeRequest()
            .withBody("{\n\t\r\"foo-foo\":2,\n\"bar\":\"\u20ac\"}")
            .fetch().as(JacksonResponse.class);
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should be 2",
                response.json().read().path("foo-foo").asInt(),
                Matchers.equalTo(2)
            ),
            () -> MatcherAssert.assertThat(
                "should be '\u20ac'",
                response.json().read().path("bar").asText(),
                Matchers.equalTo("\u20ac")
            )
        );
    }

    /**
     * JacksonResponse can read control characters.
     *
     * @throws IOException If anything goes wrong when parsing.
     */
    @Test
    void canParseUnquotedControlCharacters() throws IOException {
        MatcherAssert.assertThat(
            "should be '\u001fblah\ufffdcwhoa\u0000!'",
            new FakeRequest()
                .withBody("{\"test\":\n\"\u001fblah\ufffdcwhoa\u0000!\"}")
                .fetch().as(JacksonResponse.class)
                .json().readObject().get("test").asText(),
            Matchers.is("\u001fblah\ufffdcwhoa\u0000!")
        );
    }

    /**
     * If there's a problem parsing the body as JSON the error handling is done
     * by Jackson.
     *
     * @throws IOException If anything goes wrong.
     */
    @Test
    void invalidJsonErrorHandlingIsLeftToJackson() throws IOException {
        final JacksonResponse response = new FakeRequest()
            .withBody("{test:[]}").fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            "should complain about the missing double-quote",
            JacksonResponseTest.thrown(
                IOException.class,
                () -> response.json().read()
            ),
            Matchers.hasProperty(
                "message",
                Matchers.containsString(
                    "was expecting double-quote to start field name"
                )
            )
        );
    }

    /**
     * If there's a problem parsing the body as JSON the error handling is done
     * by Jackson.
     *
     * @throws IOException If anything goes wrong.
     */
    @Test
    void invalidJsonArrayErrorHandlingIsLeftToJackson()
        throws IOException {
        final JacksonResponse response = new FakeRequest()
            .withBody("{\"anInvalidArrayTest\":[}")
            .fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            "should complain about the unexpected close marker",
            JacksonResponseTest.thrown(
                IOException.class,
                () -> response.json().readArray()
            ),
            Matchers.hasToString(
                Matchers.containsString("Unexpected close marker")
            )
        );
    }

    /**
     * If the parsed JSON is a valid one but an array an exception is raised.
     *
     * @throws IOException If anything goes wrong.
     */
    @Test
    void cannotReadJsonAsArrayIfNotOne() throws IOException {
        final JacksonResponse response = new FakeRequest()
            .withBody("{\"objectIsNotArray\": \"It's not!\"}")
            .fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            "should complain about the object not being an array",
            JacksonResponseTest.thrown(
                IOException.class,
                () -> response.json().readArray()
            ),
            Matchers.hasToString(
                Matchers.containsString(
                    "Cannot read as an array. The JSON is not a valid array."
                )
            )
        );
    }

    /**
     * Can retrieve the JSON as an array node if it's a valid one.
     *
     * @throws IOException If anything goes wrong.
     */
    @Test
    void canReadAsArrayIfOne() throws IOException {
        final ArrayNode array = new FakeRequest()
            .withBody("[\"one\", \"two\"]")
            .fetch().as(JacksonResponse.class)
            .json().readArray();
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should be 'one'", array.get(0).asText(), Matchers.is("one")
            ),
            () -> MatcherAssert.assertThat(
                "should be 'two'", array.get(1).asText(), Matchers.is("two")
            )
        );
    }

    /**
     * If there's a problem parsing the body as JSON the error handling is done
     * by Jackson.
     *
     * @throws IOException If anything goes wrong.
     */
    @Test
    void invalidJsonObjectErrorIsLeftToJackson() throws IOException {
        final JacksonResponse response = new FakeRequest()
            .withBody("{\"anInvalidObjectTest\":{}")
            .fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            "should complain about the missing close marker",
            JacksonResponseTest.thrown(
                IOException.class,
                () -> response.json().readObject()
            ),
            Matchers.hasToString(
                Matchers.containsString(
                    "Unexpected end-of-input: expected close marker for Object"
                )
            )
        );
    }

    /**
     * If the parsed JSON is a valid one but an object an exception is raised.
     *
     * @throws IOException If anything goes wrong.
     */
    @Test
    void cannotReadJsonAsObjectIfNotOne() throws IOException {
        final JacksonResponse response = new FakeRequest()
            .withBody("[\"arrayIsNotObject\", \"It's not!\"]")
            .fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            "should complain about the array not being an object",
            JacksonResponseTest.thrown(
                IOException.class,
                () -> response.json().readObject()
            ),
            Matchers.hasToString(
                Matchers.containsString(
                    "Cannot read as an object. The JSON is not a valid object."
                )
            )
        );
    }

    /**
     * Can retrieve the JSON as an object node if it's a valid one.
     *
     * @throws IOException If anything goes wrong.
     */
    @Test
    void canReadAsObjectIfOne() throws IOException {
        MatcherAssert.assertThat(
            "should contains 'Got milk?'",
            new FakeRequest()
                .withBody("{\"hooray\": \"Got milk?\"}")
                .fetch().as(JacksonResponse.class)
                .json().readObject().get("hooray").asText(),
            Matchers.is("Got milk?")
        );
    }

    /**
     * The exception thrown by the given executable.
     * @param type Expected type of the exception
     * @param exec The executable that must throw
     * @return The exception thrown
     */
    private static Throwable thrown(final Class<? extends Throwable> type,
        final Executable exec) {
        return Assertions.assertThrows(type, exec);
    }
}
