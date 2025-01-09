/*
 * Copyright (c) 2011-2025, jcabi.com
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
package com.jcabi.http;

import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import lombok.SneakyThrows;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Template for generic tests for {@link Request}.
 *
 * @since 1.17.4
 */
@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
abstract class RequestTestTemplate {
    /**
     * Annotation for a parameterized test case.
     *
     * @since 1.17.4
     */
    @Retention(RetentionPolicy.RUNTIME)
    @ValueSource(classes = {ApacheRequest.class, JdkRequest.class})
    protected @interface Values {
    }

    /**
     * Make a request.
     * @param uri URI to start with
     * @param type Type of the request
     * @return Request
     */
    @SneakyThrows
    static Request request(final URI uri, final Class<? extends Request> type) {
        try {
            return type.getDeclaredConstructor(URI.class).newInstance(uri);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException |
            NoSuchMethodException ex) {
            throw new IllegalArgumentException(ex);
        }
    }
}
