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

import com.jcabi.http.Response;
import com.jcabi.http.request.FakeRequest;
import javax.json.stream.JsonParsingException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * Test case for {@link JsonResponse}.
 * @since 1.1
 */
public final class JsonResponseTest {

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
            response.json().readObject().getInt("foo-foo"),
            Matchers.equalTo(2)
        );
        MatcherAssert.assertThat(
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
