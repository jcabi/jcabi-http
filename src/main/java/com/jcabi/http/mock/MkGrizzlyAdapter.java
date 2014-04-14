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
package com.jcabi.http.mock;

import com.jcabi.log.Logger;
import com.sun.grizzly.tcp.http11.GrizzlyAdapter;
import com.sun.grizzly.tcp.http11.GrizzlyRequest;
import com.sun.grizzly.tcp.http11.GrizzlyResponse;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.commons.lang3.CharEncoding;
import org.apache.http.HttpHeaders;

/**
 * Mocker of Java Servlet container.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 */
final class MkGrizzlyAdapter extends GrizzlyAdapter {

    /**
     * The encoding to use.
     */
    private static final String ENCODING = "UTF-8";

    /**
     * The Charset to use.
     */
    private static final Charset CHARSET = Charset.forName(ENCODING);

    /**
     * Queries received.
     */
    private final transient Queue<MkQuery> queue =
        new ConcurrentLinkedQueue<MkQuery>();

    /**
     * Answers to give.
     */
    private final transient Queue<MkAnswer> answers =
        new ConcurrentLinkedQueue<MkAnswer>();

    @Override
    @SuppressWarnings({ "PMD.AvoidCatchingThrowable", "rawtypes" })
    public void service(final GrizzlyRequest request,
        final GrizzlyResponse response) {
        try {
            this.queue.add(new GrizzlyQuery(request));
            final MkAnswer answer = this.answers.remove();
            for (final String name : answer.headers().keySet()) {
                for (final String value : answer.headers().get(name)) {
                    response.addHeader(name, value);
                }
            }
            response.addHeader(
                HttpHeaders.SERVER,
                String.format(
                    "%s query #%d, %d answer(s) left",
                    this.getClass().getName(),
                    this.queue.size(), this.answers.size()
                )
            );
            response.setStatus(answer.status());
            final byte[] body =
                answer.body().getBytes(MkGrizzlyAdapter.CHARSET);
            response.getStream().write(body);
            response.setContentLength(body.length);
            // @checkstyle IllegalCatch (1 line)
        } catch (final Throwable ex) {
            MkGrizzlyAdapter.fail(response, ex);
        }
    }

    /**
     * Give this answer on the next request.
     * @param answer Next answer to give
     */
    public void next(final MkAnswer answer) {
        this.answers.add(answer);
    }

    /**
     * Get the oldest request received.
     * @return Request received
     */
    public MkQuery take() {
        return this.queue.remove();
    }

    /**
     * Total number of available queue.
     * @return Number of them
     */
    public int queries() {
        return this.queue.size();
    }

    /**
     * Notify this response about failure.
     * @param response The response to notify
     * @param failure The failure just happened
     */
    private static void fail(final GrizzlyResponse<?> response,
        final Throwable failure) {
        response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        final PrintWriter writer;
        try {
            writer = new PrintWriter(
                new OutputStreamWriter(
                    response.getStream(),
                    CharEncoding.UTF_8
                )
            );
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        try {
            writer.print(Logger.format("%[exception]s", failure));
        } finally {
            writer.close();
        }
    }

}
