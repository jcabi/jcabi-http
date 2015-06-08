/**
 * Copyright (c) 2011-2015, jcabi.com
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
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.JcabiHttp;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import com.jcabi.http.Response;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;

/**
 * Default implementation of {@link com.jcabi.http.Response}.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 */
@Immutable
@EqualsAndHashCode(of = { "req", "code", "phrase", "hdrs", "content" })
@Loggable(Loggable.DEBUG)
final class DefaultResponse implements Response {

    /**
     * Request.
     */
    private final transient Request req;

    /**
     * Status code.
     */
    private final transient int code;

    /**
     * Reason phrase.
     */
    private final transient String phrase;

    /**
     * Headers.
     */
    private final transient Array<Map.Entry<String, String>> hdrs;

    /**
     * Content received.
     */
    @Immutable.Array
    private final transient byte[] content;

    /**
     * Public ctor.
     * @param request The request
     * @param status HTTP status
     * @param reason HTTP reason phrase
     * @param headers HTTP headers
     * @param body Body of HTTP response
     * @checkstyle ParameterNumber (5 lines)
     */
    DefaultResponse(final Request request, final int status,
        final String reason, final Array<Map.Entry<String, String>> headers,
        final byte[] body) {
        this.req = request;
        this.code = status;
        this.phrase = reason;
        this.hdrs = headers;
        this.content = body.clone();
    }

    @Override
    @NotNull
    public Request back() {
        return this.req;
    }

    @Override
    public int status() {
        return this.code;
    }

    @Override
    public String reason() {
        return this.phrase;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Map<String, List<String>> headers() {
        final ConcurrentMap<String, List<String>> map =
            new ConcurrentHashMap<String, List<String>>(0);
        for (final Map.Entry<String, String> header : this.hdrs) {
            map.putIfAbsent(header.getKey(), new LinkedList<String>());
            map.get(header.getKey()).add(header.getValue());
        }
        return map;
    }

    @Override
    public String body() {
        final String body = new String(this.content, JcabiHttp.CHARSET);
        if (body.contains(JcabiHttp.ERR)) {
            throw new IllegalStateException(
                Logger.format(
                    "broken Unicode text at line #%d in '%[text]s' (%d bytes)",
                    body.length() - body.replace("\n", "").length(),
                    body,
                    this.content.length
                )
            );
        }
        return body;
    }

    @Override
    public byte[] binary() {
        return this.content.clone();
    }
    // @checkstyle MethodName (4 lines)
    @Override
    @SuppressWarnings("PMD.ShortMethodName")
    public <T extends Response> T as(final Class<T> type) {
        try {
            return type.getDeclaredConstructor(Response.class)
                .newInstance(this);
        } catch (final InstantiationException ex) {
            throw new IllegalStateException(ex);
        } catch (final IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } catch (final InvocationTargetException ex) {
            throw new IllegalStateException(ex);
        } catch (final NoSuchMethodException ex) {
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String toString() {
        final StringBuilder text = new StringBuilder(0)
            .append(this.code).append(' ')
            .append(this.phrase)
            .append(" [")
            .append(this.back().uri().get())
            .append("]\n");
        for (final Map.Entry<String, String> header : this.hdrs) {
            text.append(
                Logger.format(
                    "%s: %s\n",
                    header.getKey(),
                    header.getValue()
                )
            );
        }
        return text.append('\n')
            .append(new RequestBody.Printable(this.content))
            .toString();
    }

}
