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

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonReader;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * JSON response.
 *
 * <p>This response decorator is able to parse HTTP response body as
 * a JSON document and manipulate with it afterwords, for example:
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
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class JsonResponse extends AbstractResponse {

    /**
     * Public ctor.
     * @param resp Response
     */
    public JsonResponse(@NotNull(message = "response can't be NULL")
        final Response resp) {
        super(resp);
    }

    /**
     * Verifies the JSON data against the element identifier argument,
     * and throws {@link AssertionError} in case of mismatch.
     * @param element Element in the JSON data of this object
     * @return This object
     */
    @NotNull(message = "JSON response is never NULL")
    public JsonResponse assertJson(
        @NotNull(message = "JSON query can't be NULL")
        final String element) {
        throw new UnsupportedOperationException(
            // @checkstyle LineLength (1 line)
            "assertJson() is not implemented yet, since we are not sure which JSON query standard to use"
        );
    }

    /**
     * Read body as JSON.
     * @return Json reader
     */
    @NotNull(message = "JSON reader is never NULL")
    public JsonReader json() {
        return Json.createReader(new StringReader(this.body()));
    }

}
