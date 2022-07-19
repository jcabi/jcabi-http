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
package com.jcabi.http.mock;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import org.hamcrest.Matcher;

/**
 * Mock version of Java Servlet container.
 *
 * <p>A convenient tool to test your application classes against a web
 * service. For example:
 *
 * <pre> MkContainer container = new MkGrizzlyContainer()
 *   .next(new MkAnswer.Simple(200, "works fine!"))
 *   .start();
 * new JdkRequest(container.home())
 *   .header("Accept", "text/xml")
 *   .fetch().as(RestResponse.class)
 *   .assertStatus(200)
 *   .assertBody(Matchers.equalTo("works fine!"));
 * MatcherAssert.assertThat(
 *   container.take().method(),
 *   Matchers.equalTo("GET")
 * );
 * container.stop();</pre>
 *
 * <p>Keep in mind that container automatically reserves a new free TCP port
 * and works until JVM is shut down. The only way to stop it is to call
 * {@link #stop()}.
 *
 * <p>Since version 0.11 container implements {@link Closeable} and can be
 * used in try-with-resource block.
 *
 * @since 0.10
 * @see <a href="http://www.rexsl.com/rexsl-test/example-mock-servlet.html">Examples</a>
 */
@SuppressWarnings("PMD.TooManyMethods")
public interface MkContainer extends Closeable {

    /**
     * Give this answer on the next request.
     * @param answer Next answer to give
     * @return This object
     */
    MkContainer next(MkAnswer answer);

    /**
     * Give this answer on the next request if the matcher condition is
     * satisfied.
     * @param answer Next answer to give
     * @param condition The condition to match
     * @return This object
     */
    MkContainer next(MkAnswer answer, Matcher<MkQuery> condition);

    /**
     * Give this answer on the next request(s) if the matcher condition is
     * satisfied up to a certain number of requests.
     * @param answer Next answer to give
     * @param condition The condition to match
     * @param count Number of requests to match
     * @return This object
     */
    MkContainer next(MkAnswer answer, Matcher<MkQuery> condition, int count);

    /**
     * Get the oldest request received
     * ({@link java.util.NoSuchElementException}
     * if no more elements in the list).
     * @return Request received
     */
    MkQuery take();

    /**
     * Get the oldest request received subject to the matching condition.
     * ({@link java.util.NoSuchElementException} if no elements satisfy the
     * condition).
     * @param matcher The matcher specifying the condition
     * @return Request received satisfying the matcher
     */
    MkQuery take(Matcher<MkAnswer> matcher);

    /**
     * Get the all requests received satisfying the given matcher.
     * ({@link java.util.NoSuchElementException} if no elements satisfy the
     * condition).
     * @param matcher The matcher specifying the condition
     * @return Collection of all requests satisfying the matcher, ordered from
     *  oldest to newest.
     */
    Collection<MkQuery> takeAll(Matcher<MkAnswer> matcher);

    /**
     * How many queries we have left.
     * @return Total number of queries you can retrieve with {@link #take()}
     * @since 1.0
     */
    int queries();

    /**
     * Start it on the first available TCP port.
     * @return This object
     * @throws IOException If fails
     */
    MkContainer start() throws IOException;

    /**
     * Start it on a provided port.
     * @param prt The port where it should start listening
     * @return This object
     * @throws IOException If fails
     */
    MkContainer start(int prt) throws IOException;

    /**
     * Stop container.
     */
    void stop();

    /**
     * Get its home.
     * @return URI of the started container
     */
    URI home();

}
