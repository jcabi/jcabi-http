/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

package com.jcabi.http;

import com.jcabi.http.request.BaseRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

/**
 * Test case for loss of timeout parameters.
 * @since 1.17.3
 */
@SuppressWarnings("PMD.TooManyMethods")
final class RequestTimeoutLossTest extends RequestTestTemplate {
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
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeBody(
        final Class<? extends Request> type
    )
        throws Exception {
        final Callable<Response> execution = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET)
                    .timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .body()
                    .back()
                    .fetch();
            }
        };
        this.testTimeoutOrderDoesntMatter(execution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeFetch(
        final Class<? extends Request> type
    )
        throws Exception {
        final Callable<Response> execution = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET)
                    .timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .fetch();
            }
        };
        this.testTimeoutOrderDoesntMatter(execution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeHeader(
        final Class<? extends Request> type
    )
        throws Exception {
        final Callable<Response> execution = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET)
                    .timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .header(
                        RequestTimeoutLossTest.CONTENT_TYPE,
                        "text/plain"
                    )
                    .fetch();
            }
        };
        this.testTimeoutOrderDoesntMatter(execution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeMethod(
        final Class<? extends Request> type
    )
        throws Exception {
        final Callable<Response> execution = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .method(Request.GET)
                    .fetch();
            }
        };
        this.testTimeoutOrderDoesntMatter(execution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeMultipartBody(
        final Class<? extends Request> type
    )
        throws Exception {
        final Callable<Response> execution = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET)
                    .timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .multipartBody()
                    .back()
                    .fetch();
            }
        };
        this.testTimeoutOrderDoesntMatter(execution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeReset(
        final Class<? extends Request> type
    )
        throws Exception {
        final Callable<Response> execution = new Callable<Response>() {
            @Override
            public Response call() throws Exception {
                return RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET)
                    .timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .reset(RequestTimeoutLossTest.CONTENT_TYPE)
                    .fetch();
            }
        };
        this.testTimeoutOrderDoesntMatter(execution);
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeUriBack(
        final Class<? extends Request> type
    )
        throws Exception {
        this.testTimeoutOrderDoesntMatter(
            new Callable<Response>() {
                @Override
                public Response call() throws Exception {
                    return RequestTimeoutLossTest.request(type)
                        .through(MockWire.class)
                        .method(Request.GET)
                        .timeout(
                            RequestTimeoutLossTest.CONNECT_TIMEOUT,
                            RequestTimeoutLossTest.READ_TIMEOUT
                        )
                        .uri()
                        .path("/api")
                        .back()
                        .fetch();
                }
            }
        );
    }

    /**
     * The wire passed to method "through" is used.
     *
     * @throws IOException On error
     */
    @Test
    void passesThroughWire() throws IOException {
        final Wire original = Mockito.mock(Wire.class);
        final Wire wire = Mockito.mock(Wire.class);
        final Response response = Mockito.mock(Response.class);
        final Supplier<Collection<Map.Entry<String, String>>> hdrs =
            new Supplier<Collection<Map.Entry<String, String>>>() {
                @Override
                public Collection<Map.Entry<String, String>> get() {
                    return ArgumentMatchers.anyCollection();
                }
            };
        final String url = "fake-url";
        Mockito.when(
            wire.send(
                ArgumentMatchers.any(Request.class),
                ArgumentMatchers.eq(url),
                ArgumentMatchers.anyString(),
                hdrs.get(),
                ArgumentMatchers.any(InputStream.class),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(response);
        new BaseRequest(original, url).through(wire).fetch();
        Mockito.verify(original, Mockito.never()).send(
            ArgumentMatchers.any(Request.class),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            hdrs.get(),
            ArgumentMatchers.any(InputStream.class),
            ArgumentMatchers.anyInt(),
            ArgumentMatchers.anyInt()
        );
        Mockito.verify(wire).send(
            ArgumentMatchers.any(Request.class),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            hdrs.get(),
            ArgumentMatchers.any(InputStream.class),
            ArgumentMatchers.anyInt(),
            ArgumentMatchers.anyInt()
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * <code>Request.timeout</code> is called.
     *
     * @param exec The callable that contains the actual request
     * @throws Exception If something goes wrong inside
     */
    @SuppressWarnings("unchecked")
    private void testTimeoutOrderDoesntMatter(final Callable<Response> exec)
        throws Exception {
        synchronized (MockWire.class) {
            final Wire wire = Mockito.mock(Wire.class);
            final ArgumentCaptor<Integer> cnc = ArgumentCaptor
                .forClass(Integer.class);
            final ArgumentCaptor<Integer> rdc = ArgumentCaptor
                .forClass(Integer.class);
            MockWire.setMockDelegate(wire);
            final Response response = Mockito.mock(Response.class);
            Mockito.when(
                wire.send(
                    Mockito.any(Request.class),
                    Mockito.anyString(),
                    Mockito.anyString(),
                    Mockito.<Map.Entry<String, String>>anyCollection(),
                    Mockito.any(InputStream.class),
                    Mockito.anyInt(),
                    Mockito.anyInt()
                )
            ).thenReturn(response);
            exec.call();
            Mockito.verify(wire).send(
                Mockito.any(Request.class),
                Mockito.anyString(),
                Mockito.anyString(),
                Mockito.<Map.Entry<String, String>>anyCollection(),
                Mockito.any(InputStream.class),
                cnc.capture(),
                rdc.capture()
            );
            MatcherAssert.assertThat(
                "should be connect timeout",
                cnc.getValue().intValue(),
                Matchers.is(RequestTimeoutLossTest.CONNECT_TIMEOUT)
            );
            MatcherAssert.assertThat(
                "should be read timeout",
                rdc.getValue().intValue(),
                Matchers.is(RequestTimeoutLossTest.READ_TIMEOUT)
            );
        }
    }

    /**
     * Make a request with default url.
     * @param type Type of Request
     * @return Request
     * @throws Exception If fails
     */
    private static Request request(final Class<? extends Request> type)
        throws Exception {
        return RequestTestTemplate.request(
            new URI(RequestTimeoutLossTest.LOCALHOST_URL),
            type
        );
    }

}
