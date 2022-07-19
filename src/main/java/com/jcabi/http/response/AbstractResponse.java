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

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Abstract response.
 *
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(of = "response")
abstract class AbstractResponse implements Response {

    /**
     * Encapsulated response.
     */
    private final transient Response response;

    /**
     * Ctor.
     * @param resp Response
     */
    AbstractResponse(final Response resp) {
        this.response = resp;
    }

    @Override
    public final String toString() {
        return this.response.toString();
    }

    @Override
    public final Request back() {
        return this.response.back();
    }

    @Override
    public final int status() {
        return this.response.status();
    }

    @Override
    public final String reason() {
        return this.response.reason();
    }

    @Override
    public final Map<String, List<String>> headers() {
        return this.response.headers();
    }

    @Override
    public String body() {
        return this.response.body();
    }

    @Override
    public final byte[] binary() {
        return this.response.binary();
    }

    // @checkstyle MethodName (4 lines)
    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    public final <T extends Response> T as(final Class<T> type) {
        return this.response.as(type);
    }

}
