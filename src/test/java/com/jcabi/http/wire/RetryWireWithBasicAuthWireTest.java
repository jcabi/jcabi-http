/**
 * Copyright (c) 2011-2015, jcabi.com
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
package com.jcabi.http.wire;

import com.google.common.collect.Lists;
import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Test case for combining Retry and BasicAuth Wires
 *
 * @author Dom Farr (dominicfarr@gmail.com)
 * @version $Id$
 */
public final class RetryWireWithBasicAuthWireTest {
    final String username = "harry-potter";
    final String password = "silly-wizard";

    // sorry I've removed your final modifier to extend this as simple as I can.
    // And now I've written a comment breaking two rules in process.
    // Boo
    private final MkContainer container = new MkGrizzlyContainer() {

        @Override
        public URI home() {
            URI home = super.home();
            return URI.create(home.toString().replace("://", String.format("://%s:%s@", username, password)));
        }
    };

    private InMemoryLogCapture inMemoryLogCapture;


    @Before
    public void buildAppenderContext() {
        inMemoryLogCapture = new InMemoryLogCapture();
        inMemoryLogCapture.setThreshold(Level.WARN);
        LogManager.getRootLogger().addAppender(inMemoryLogCapture);
    }

    @Test
    public void makesMultipleRequestsButDoNotExposeSensitiveBasicAuthenticationInformationInLogging() throws Exception {
        container.next(new MkAnswer.Simple(HttpURLConnection.HTTP_INTERNAL_ERROR))
                .next(new MkAnswer.Simple(HttpURLConnection.HTTP_OK))
                .start();
        new JdkRequest(container.home())
                .through(BasicAuthWire.class)
                .through(RetryWire.class)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        container.stop();

        for (LoggingEvent logEvent : inMemoryLogCapture.showMeTheLogging()) {
            if (((String) logEvent.getMessage()).contains(username) || ((String) logEvent.getMessage()).contains(password))
                fail("Logging should not contain username or password");
        }
    }

    // am i really testing logging?
    private final class InMemoryLogCapture extends AppenderSkeleton {

        public List<LoggingEvent> logs = Lists.newArrayList();

        public List<LoggingEvent> showMeTheLogging() {
            return Collections.unmodifiableList(logs);
        }

        @Override
        protected void append(LoggingEvent loggingEvent) {
            logs.add(loggingEvent);
        }

        @Override
        public void close() {

        }

        @Override
        public boolean requiresLayout() {
            return false;
        }
    }
}
