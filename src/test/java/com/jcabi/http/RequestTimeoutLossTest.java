/**
 * Copyright (c) 2011-2017, jcabi.com
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

import com.google.common.base.Supplier;
import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.BaseRequest;
import com.jcabi.http.request.JdkRequest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Test case for loss of timeout parameters.
 * @author Jakob Oswald (jakob.oswald@gmx.net)
 * @version $Id$
 */
@SuppressWarnings({
        "PMD.TooManyMethods", "PMD.DoNotUseThreads",
        "PMD.AvoidCatchingGenericException",
        "PMD.AvoidThrowingRawExceptionTypes"
    })
@RunWith(Parameterized.class)
public final class RequestTimeoutLossTest {
    /**
     * Placeholder URL used for testing purposes only.
     */
    private static final String LOCALHOST_URL = "http://localhost";

    /**
     * Content type header name for testing purposes only.
     */
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * Magic number for connection timeout.
     */
    private static final int CONNECT_TIMEOUT = 1234;

    /**
     * Magic number for read timeout.
     */
    private static final int READ_TIMEOUT = 2345;

    /**
     * Type of request.
     */
    private final transient Class<? extends Request> type;

    /**
     * Public ctor.
     * @param req Request type
     */
    public RequestTimeoutLossTest(final Class<? extends Request> req) {
        this.type = req;
    }

    /**
     * Parameters.
     * @return Array of args
     */
    @Parameterized.Parameters
    public static Collection<Object[]> primeNumbers() {
        return Arrays.asList(
                new Object[]{ApacheRequest.class},
                new Object[]{JdkRequest.class}
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTimeoutOrderDoesntMatterBeforeBody()
            throws Exception {
        final Runnable requestExecution = new Runnable() {
            @Override
            public void run() {
                try {
                    // @checkstyle RequireThisCheck (1 lines)
                    request(new URI(LOCALHOST_URL))
                            .through(MockWire.class)
                            .method(Request.GET)
                            .timeout(CONNECT_TIMEOUT, READ_TIMEOUT)
                            .body()
                            .back()
                            .fetch();
                    // @checkstyle IllegalCatchCheck (1 lines)
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        this.testTimeoutOrderDoesntMatter(requestExecution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTimeoutOrderDoesntMatterBeforeFetch()
            throws Exception {
        final Runnable requestExecution = new Runnable() {
            @Override
            public void run() {
                try {
                    // @checkstyle RequireThisCheck (1 lines)
                    request(new URI(LOCALHOST_URL))
                            .through(MockWire.class)
                            .method(Request.GET)
                            .timeout(CONNECT_TIMEOUT, READ_TIMEOUT)
                            .fetch();
                    // @checkstyle IllegalCatchCheck (1 lines)
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        this.testTimeoutOrderDoesntMatter(requestExecution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTimeoutOrderDoesntMatterBeforeHeader()
            throws Exception {
        final Runnable requestExecution = new Runnable() {
            @Override
            public void run() {
                try {
                    // @checkstyle RequireThisCheck (1 lines)
                    request(new URI(LOCALHOST_URL))
                            .through(MockWire.class)
                            .method(Request.GET)
                            .timeout(CONNECT_TIMEOUT, READ_TIMEOUT)
                            .header(CONTENT_TYPE, "text/plain")
                            .fetch();
                    // @checkstyle IllegalCatchCheck (1 lines)
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        this.testTimeoutOrderDoesntMatter(requestExecution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTimeoutOrderDoesntMatterBeforeMethod()
            throws Exception {
        final Runnable requestExecution = new Runnable() {
            @Override
            public void run() {
                try {
                    // @checkstyle RequireThisCheck (1 lines)
                    request(new URI(LOCALHOST_URL))
                            .through(MockWire.class)
                            .timeout(CONNECT_TIMEOUT, READ_TIMEOUT)
                            .method(Request.GET)
                            .fetch();
                    // @checkstyle IllegalCatchCheck (1 lines)
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        this.testTimeoutOrderDoesntMatter(requestExecution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTimeoutOrderDoesntMatterBeforeMultipartBody()
            throws Exception {
        final Runnable requestExecution = new Runnable() {
            @Override
            public void run() {
                try {
                    // @checkstyle RequireThisCheck (1 lines)
                    request(new URI(LOCALHOST_URL))
                            .through(MockWire.class)
                            .method(Request.GET)
                            .timeout(CONNECT_TIMEOUT, READ_TIMEOUT)
                            .multipartBody()
                            .back()
                            .fetch();
                    // @checkstyle IllegalCatchCheck (1 lines)
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        this.testTimeoutOrderDoesntMatter(requestExecution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTimeoutOrderDoesntMatterBeforeReset()
            throws Exception {
        final Runnable requestExecution = new Runnable() {
            @Override
            public void run() {
                try {
                    // @checkstyle RequireThisCheck (1 lines)
                    request(new URI(LOCALHOST_URL))
                            .through(MockWire.class)
                            .method(Request.GET)
                            .timeout(CONNECT_TIMEOUT, READ_TIMEOUT)
                            .reset(CONTENT_TYPE)
                            .fetch();
                    // @checkstyle IllegalCatchCheck (1 lines)
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        this.testTimeoutOrderDoesntMatter(requestExecution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @throws Exception If something goes wrong inside
     */
    @Test
    public void testTimeoutOrderDoesntMatterBeforeUriBack()
            throws Exception {
        final Runnable requestExecution = new Runnable() {
            @Override
            public void run() {
                try {
                    // @checkstyle RequireThisCheck (1 lines)
                    request(new URI(LOCALHOST_URL))
                            .through(MockWire.class)
                            .method(Request.GET)
                            .timeout(CONNECT_TIMEOUT, READ_TIMEOUT)
                            .uri()
                            .path("/api")
                            .back()
                            .fetch();
                    // @checkstyle IllegalCatchCheck (1 lines)
                } catch (final Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        this.testTimeoutOrderDoesntMatter(requestExecution);
    }

    /**
     * The wire passed to method "through" is used.
     *
     * @throws IOException On error
     */
    @Test
    public void passesThroughWire() throws IOException {
        final Wire original = Mockito.mock(Wire.class);
        final Wire wire = Mockito.mock(Wire.class);
        final Response response = Mockito.mock(Response.class);
        final Supplier<Collection<Map.Entry<String, String>>> hdrs =
            new Supplier<Collection<Map.Entry<String, String>>>() {
                @Override
                public Collection<Map.Entry<String, String>> get() {
                    return org.mockito.Matchers.anyCollectionOf(null);
            }
        };
        final String url = "fake-url";
        Mockito.when(
                wire.send(
                        org.mockito.Matchers.any(Request.class),
                        org.mockito.Matchers.eq(url),
                        org.mockito.Matchers.anyString(),
                        hdrs.get(),
                        org.mockito.Matchers.any(InputStream.class),
                        org.mockito.Matchers.anyInt(),
                        org.mockito.Matchers.anyInt(),
                        org.mockito.Matchers.any(SSLContext.class)
                )
        ).thenReturn(response);
        new BaseRequest(original, url).through(wire).fetch();
        Mockito.verify(original, Mockito.never()).send(
                org.mockito.Matchers.any(Request.class),
                org.mockito.Matchers.anyString(),
                org.mockito.Matchers.anyString(),
                hdrs.get(),
                org.mockito.Matchers.any(InputStream.class),
                org.mockito.Matchers.anyInt(),
                org.mockito.Matchers.anyInt(),
                org.mockito.Matchers.any(SSLContext.class)
        );
        Mockito.verify(wire).send(
                org.mockito.Matchers.any(Request.class),
                org.mockito.Matchers.anyString(),
                org.mockito.Matchers.anyString(),
                hdrs.get(),
                org.mockito.Matchers.any(InputStream.class),
                org.mockito.Matchers.anyInt(),
                org.mockito.Matchers.anyInt(),
                org.mockito.Matchers.any(SSLContext.class)
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param execution The runnable that contains the actual request execution
     * @throws Exception If something goes wrong inside
     */
    @SuppressWarnings("unchecked")
    private void testTimeoutOrderDoesntMatter(final Runnable execution)
            throws Exception {
        synchronized (MockWire.class) {
            final Wire mockWire = Mockito.mock(Wire.class);
            final ArgumentCaptor<Integer> connectCaptor = ArgumentCaptor
                    .forClass(Integer.class);
            final ArgumentCaptor<Integer> readCaptor = ArgumentCaptor
                    .forClass(Integer.class);
            MockWire.setMockDelegate(mockWire);
            final Response mockResponse = Mockito.mock(Response.class);
            Mockito.when(
                    mockWire.send(
                            Mockito.any(Request.class),
                            Mockito.anyString(),
                            Mockito.anyString(),
                            Mockito.<Map.Entry<String, String>>anyCollection(),
                            Mockito.any(InputStream.class),
                            Mockito.anyInt(),
                            Mockito.anyInt(),
                            org.mockito.Matchers.any(SSLContext.class)
                    )
            ).thenReturn(mockResponse);
            execution.run();
            Mockito.verify(mockWire).send(
                    Mockito.any(Request.class),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.<Map.Entry<String, String>>anyCollection(),
                    Mockito.any(InputStream.class),
                    connectCaptor.capture(),
                    readCaptor.capture(),
                    org.mockito.Matchers.any(SSLContext.class)
            );
            MatcherAssert.assertThat(
                    connectCaptor.getValue().intValue(),
                    Matchers.is(CONNECT_TIMEOUT)
            );
            MatcherAssert.assertThat(
                    readCaptor.getValue().intValue(),
                    Matchers.is(READ_TIMEOUT)
            );
        }
    }

    /**
     * Make a request.
     * @param uri URI to start with
     * @return Request
     * @throws Exception If fails
     */
    private Request request(final URI uri) throws Exception {
        return this.type.getDeclaredConstructor(URI.class).newInstance(uri);
    }

}
