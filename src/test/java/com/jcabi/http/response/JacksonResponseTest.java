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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        MatcherAssert.assertThat(
            response.json().read().path("foo-foo").asInt(),
            Matchers.equalTo(2)
        );
        MatcherAssert.assertThat(
            response.json().read().path("bar").asText(),
            Matchers.equalTo("\u20ac")
        );
    }

    /**
     * JacksonResponse can read control characters.
     *
     * @throws IOException If anything goes wrong when parsing.
     */
    @Test
    void canParseUnquotedControlCharacters() throws IOException {
        final JacksonResponse response = new FakeRequest()
            .withBody("{\"test\":\n\"\u001Fblah\uFFFDcwhoa\u0000!\"}")
            .fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            response.json().readObject().get("test").asText(),
            Matchers.is("\u001Fblah\uFFFDcwhoa\u0000!")
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
        final String body = "{test:[]}";
        final String err = "was expecting double-quote to start field name";
        final JacksonResponse response = new FakeRequest()
            .withBody(body).fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IOException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        response.json().read();
                    }
                }
            ),
            Matchers.hasProperty(
                "message",
                Matchers.containsString(err)
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
            Assertions.assertThrows(
                IOException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        response.json().readArray();
                    }
                }
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
            Assertions.assertThrows(
                IOException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        response.json().readArray();
                    }
                }
            ),
            Matchers.<IOException>hasToString(
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
        final JacksonResponse response = new FakeRequest()
            .withBody("[\"one\", \"two\"]")
            .fetch().as(JacksonResponse.class);
        final ArrayNode array = response.json().readArray();
        MatcherAssert.assertThat(
            array.get(0).asText(), Matchers.is("one")
        );
        MatcherAssert.assertThat(
            array.get(1).asText(), Matchers.is("two")
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
            Assertions.assertThrows(
                IOException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        response.json().readObject();
                    }
                }
            ),
            Matchers.<IOException>hasToString(
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
        final String body = "[\"arrayIsNotObject\", \"It's not!\"]";
        final JacksonResponse response = new FakeRequest()
            .withBody(body).fetch().as(JacksonResponse.class);
        MatcherAssert.assertThat(
            Assertions.assertThrows(
                IOException.class,
                new Executable() {
                    @Override
                    public void execute() throws Throwable {
                        response.json().readObject();
                    }
                }
            ),
            Matchers.<IOException>hasToString(
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
        final JacksonResponse response = new FakeRequest()
            .withBody("{\"hooray\": \"Got milk?\"}")
            .fetch().as(JacksonResponse.class);
        final ObjectNode object = response.json().readObject();
        MatcherAssert.assertThat(
            object.get("hooray").asText(), Matchers.is("Got milk?")
        );
    }
}
