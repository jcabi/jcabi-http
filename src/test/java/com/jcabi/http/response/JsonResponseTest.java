/**
 * Copyright (c) 2011-2014, JCabi.com
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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Test case for {@link JsonResponse}.
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
public final class JsonResponseTest {

    /**
     * JsonResponse can read and return a JSON document.
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void readsJsonDocument() throws Exception {
        final Response resp = new FakeRequest()
            .withBody("{\"foo-foo\":2, \"bar\":\"hello!\"}")
            .fetch();
        final JsonResponse response = new JsonResponse(resp);
        MatcherAssert.assertThat(
            response.json().readObject().getInt("foo-foo"),
            Matchers.equalTo(2)
        );
        MatcherAssert.assertThat(
            response.json().readObject().getString("bar"),
            Matchers.equalTo("hello!")
        );
    }

    /**
     * JsonResponse can read control characters.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void readsControlCharacters() throws Exception {
        final Response resp = new FakeRequest()
            .withBody("{\"test\": \"\u001Fblah\u0001cwhoa\u0000!\"}").fetch();
        final JsonResponse response = new JsonResponse(resp);
        MatcherAssert.assertThat(
            response.json().readObject().getString("test"),
            Matchers.is("\u001Fblah\u0001cwhoa\u0000!")
        );
    }

}
