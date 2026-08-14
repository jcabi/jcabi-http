/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */

package com.jcabi.http;

import com.jcabi.http.request.BaseRequest;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
     * Guard of the static mock delegate of {@link MockWire}.
     */
    private static final Lock LOCK = new ReentrantLock();

    /**
     * The connect and read timeouts are properly set no matter in which order
     * {@code Request.timeout} is called.
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeBody(
        final Class<? extends Request> type
    ) throws Exception {
        MatcherAssert.assertThat(
            "should keep timeouts set before body",
            RequestTimeoutLossTest.timeouts(
                () -> RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET).timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .body()
                    .back()
                    .fetch()
            ),
            Matchers.contains(
                RequestTimeoutLossTest.CONNECT_TIMEOUT,
                RequestTimeoutLossTest.READ_TIMEOUT
            )
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * {@code Request.timeout} is called.
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeFetch(
        final Class<? extends Request> type
    ) throws Exception {
        MatcherAssert.assertThat(
            "should keep timeouts set before fetch",
            RequestTimeoutLossTest.timeouts(
                () -> RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET).timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .fetch()
            ),
            Matchers.contains(
                RequestTimeoutLossTest.CONNECT_TIMEOUT,
                RequestTimeoutLossTest.READ_TIMEOUT
            )
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * {@code Request.timeout} is called.
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeHeader(
        final Class<? extends Request> type
    ) throws Exception {
        MatcherAssert.assertThat(
            "should keep timeouts set before header",
            RequestTimeoutLossTest.timeouts(
                () -> RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET).timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .header(RequestTimeoutLossTest.CONTENT_TYPE, "text/plain")
                    .fetch()
            ),
            Matchers.contains(
                RequestTimeoutLossTest.CONNECT_TIMEOUT,
                RequestTimeoutLossTest.READ_TIMEOUT
            )
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * {@code Request.timeout} is called.
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeMethod(
        final Class<? extends Request> type
    ) throws Exception {
        MatcherAssert.assertThat(
            "should keep timeouts set before method",
            RequestTimeoutLossTest.timeouts(
                () -> RequestTimeoutLossTest.request(type)
                    .through(MockWire.class).timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .method(Request.GET)
                    .fetch()
            ),
            Matchers.contains(
                RequestTimeoutLossTest.CONNECT_TIMEOUT,
                RequestTimeoutLossTest.READ_TIMEOUT
            )
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * {@code Request.timeout} is called.
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeMultipartBody(
        final Class<? extends Request> type
    ) throws Exception {
        MatcherAssert.assertThat(
            "should keep timeouts set before multipart body",
            RequestTimeoutLossTest.timeouts(
                () -> RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET).timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .multipartBody()
                    .back()
                    .fetch()
            ),
            Matchers.contains(
                RequestTimeoutLossTest.CONNECT_TIMEOUT,
                RequestTimeoutLossTest.READ_TIMEOUT
            )
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * {@code Request.timeout} is called.
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeReset(
        final Class<? extends Request> type
    ) throws Exception {
        MatcherAssert.assertThat(
            "should keep timeouts set before reset",
            RequestTimeoutLossTest.timeouts(
                () -> RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET).timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .reset(RequestTimeoutLossTest.CONTENT_TYPE)
                    .fetch()
            ),
            Matchers.contains(
                RequestTimeoutLossTest.CONNECT_TIMEOUT,
                RequestTimeoutLossTest.READ_TIMEOUT
            )
        );
    }

    /**
     * The connect and read timeouts are properly set no matter in which order
     * {@code Request.timeout} is called.
     * @param type Type of Request
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @Values
    void testTimeoutOrderDoesntMatterBeforeUriBack(
        final Class<? extends Request> type
    ) throws Exception {
        MatcherAssert.assertThat(
            "should keep timeouts set before uri back",
            RequestTimeoutLossTest.timeouts(
                () -> RequestTimeoutLossTest.request(type)
                    .through(MockWire.class)
                    .method(Request.GET).timeout(
                        RequestTimeoutLossTest.CONNECT_TIMEOUT,
                        RequestTimeoutLossTest.READ_TIMEOUT
                    )
                    .uri()
                    .path("/api")
                    .back()
                    .fetch()
            ),
            Matchers.contains(
                RequestTimeoutLossTest.CONNECT_TIMEOUT,
                RequestTimeoutLossTest.READ_TIMEOUT
            )
        );
    }

    /**
     * The wire passed to method "through" is used.
     * @throws IOException On error
     */
    @Test
    void passesThroughWire() throws IOException {
        final Wire wire = Mockito.mock(Wire.class);
        final String url = "fake-url";
        Mockito.when(
            wire.send(
                ArgumentMatchers.any(Request.class),
                ArgumentMatchers.eq(url),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.<Map.Entry<String, String>>anyCollection(),
                ArgumentMatchers.any(InputStream.class),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt()
            )
        ).thenReturn(Mockito.mock(Response.class));
        new BaseRequest(
            (req, home, method, headers, content, connect, read) -> {
                throw new IllegalStateException(
                    "the original wire cannot be touched"
                );
            },
            url
        ).through(wire).fetch();
        Mockito.verify(wire).send(
            ArgumentMatchers.any(Request.class),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            ArgumentMatchers.<Map.Entry<String, String>>anyCollection(),
            ArgumentMatchers.any(InputStream.class),
            ArgumentMatchers.anyInt(),
            ArgumentMatchers.anyInt()
        );
    }

    /**
     * The connect and read timeouts the wire has been given.
     * @param exec The callable that contains the actual request
     * @return The pair of connect and read timeouts
     * @throws Exception If something goes wrong inside
     */
    @SuppressWarnings("unchecked")
    private static List<Integer> timeouts(final Callable<Response> exec)
        throws Exception {
        RequestTimeoutLossTest.LOCK.lock();
        try {
            final Wire wire = Mockito.mock(Wire.class);
            final ArgumentCaptor<Integer> cnc = ArgumentCaptor
                .forClass(Integer.class);
            final ArgumentCaptor<Integer> rdc = ArgumentCaptor
                .forClass(Integer.class);
            MockWire.setMockDelegate(wire);
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
            ).thenReturn(Mockito.mock(Response.class));
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
            return Arrays.asList(cnc.getValue(), rdc.getValue());
        } finally {
            RequestTimeoutLossTest.LOCK.unlock();
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
