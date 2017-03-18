/*
 * Copyright (c) 2011-2017, jcabi.com
 * All rights reserved.
 * <p>
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
 * <p>
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
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for {@link YmlResponse}.
 *
 * @author Khlebnikov Andrey (viruszold@gmail.com)
 * @version $Id$
 */
public final class YmlResponseTest {
    /**
     * YmlResponse can deserialize an object of some class.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void loadAs() throws Exception {
        final Response response = new FakeRequest()
                .withBody("source: empty").fetch();
        Assert.assertEquals(
                new YmlResponse(response).loadAs(SimpleYml.class).source,
                "empty"
        );
    }

    /**
     * Simple deserialize class.
     */
    static final class SimpleYml {
        /**
         * Simple field.
         */
        @SuppressWarnings({
                "checkstyle:visibilitymodifiercheck",
                "checkstyle:javadocvariablecheck"}
        )
        public String source;
    }
}
