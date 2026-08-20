/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.http.HttpHeaders;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.hamcrest.Matcher;

/**
 * Mocker of Java Servlet container.
 * @since 0.10
 */
final class MkGrizzlyAdapter extends HttpHandler {

    /**
     * Queries received.
     */
    private final transient Queue<QueryWithAnswer> queue =
        new ConcurrentLinkedQueue<>();

    /**
     * Answers to give conditionally.
     */
    private final transient Queue<Conditional> conditionals =
        new ConcurrentLinkedQueue<>();

    @Override
    @SuppressWarnings("rawtypes")
    public void service(final Request request, final Response response) {
        try {
            this.handleRequest(request, response);
        } catch (final IOException ex) {
            MkGrizzlyAdapter.fail(response, ex);
        }
    }

    /**
     * Give this answer on the next request(s) if they match the given condition
     * a certain number of consecutive times.
     * @param answer Next answer to give
     * @param query The query that should be satisfied to return this answer
     * @param count The number of times this answer can be returned for matching
     *  requests
     */
    void next(
        final MkAnswer answer, final Matcher<MkQuery> query,
        final int count
    ) {
        this.conditionals.add(
            new Conditional(answer, query, count)
        );
    }

    /**
     * Get the oldest request received.
     * @return Request received
     */
    MkQuery take() {
        return this.queue.remove().query();
    }

    /**
     * Get the oldest request received subject to the matching condition.
     * ({@link java.util.NoSuchElementException} if no elements satisfy the
     * condition).
     * @param matcher The matcher specifying the condition
     * @return Request received satisfying the matcher
     */
    MkQuery take(final Matcher<MkAnswer> matcher) {
        return this.takeMatching(matcher).next();
    }

    /**
     * Get the all requests received satisfying the given matcher.
     * ({@link java.util.NoSuchElementException} if no elements satisfy the
     * condition).
     * @param matcher The matcher specifying the condition
     * @return Collection of all requests satisfying the matcher, ordered from
     *  oldest to newest
     */
    Collection<MkQuery> takeAll(final Matcher<MkAnswer> matcher) {
        final Collection<MkQuery> results = new ArrayList<>(0);
        final Iterator<MkQuery> iter = this.takeMatching(matcher);
        while (iter.hasNext()) {
            results.add(iter.next());
        }
        return results;
    }

    /**
     * Total number of available queue.
     * @return Number of them
     */
    int queries() {
        return this.queue.size();
    }

    private Iterator<MkQuery> takeMatching(final Matcher<MkAnswer> matcher) {
        final Iterator<MkQuery> result = new MkQueryIterator(
            this.queue.iterator(), matcher
        );
        if (!result.hasNext()) {
            throw new NoSuchElementException("No matching results found");
        }
        return result;
    }

    private static void fail(
        final Response response,
        final Throwable failure
    ) {
        response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        try (
            PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                    response.createOutputStream(),
                    StandardCharsets.UTF_8
                )
            )
        ) {
            writer.print(Logger.format("%[exception]s", failure));
        }
    }

    private void handleRequest(final Request request, final Response response) throws IOException {
        if (!this.processConditionals(new GrizzlyQuery(request), response)) {
            throw new NoSuchElementException("No matching answers found.");
        }
    }

    private boolean processConditionals(final MkQuery query, final Response response) {
        final Iterator<Conditional> iter = this.conditionals.iterator();
        boolean res = false;
        while (iter.hasNext()) {
            final Conditional cond = iter.next();
            if (cond.matches(query)) {
                this.handleMatchingConditional(cond, query, response);
                if (cond.decrement() == 0) {
                    iter.remove();
                }
                res = true;
                break;
            }
        }
        return res;
    }

    private void handleMatchingConditional(
        final Conditional cond,
        final MkQuery query,
        final Response response
    ) {
        final MkAnswer answer = cond.answer();
        this.queue.add(new QueryWithAnswer(query, answer));
        addHeadersToResponse(answer.headers(), response);
        this.addServerHeader(response);
        setResponseStatusAndBody(response, answer);
    }

    private static void addHeadersToResponse(
        final Map<String, List<String>> headers,
        final Response response
    ) {
        for (final Map.Entry<String, List<String>> entry : headers.entrySet()) {
            for (final String value : entry.getValue()) {
                response.addHeader(entry.getKey(), value);
            }
        }
    }

    private void addServerHeader(final Response response) {
        response.addHeader(
            HttpHeaders.SERVER,
            String.format(
                "%s query #%d, %d answer(s) left",
                this.getClass().getName(),
                this.queue.size(), this.conditionals.size()
            )
        );
    }

    @SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
    private static void setResponseStatusAndBody(
        final Response response,
        final MkAnswer answer
    ) {
        response.setStatus(answer.status());
        final byte[] body = answer.bodyBytes();
        try {
            response.createOutputStream().write(body);
        } catch (final IOException ex) {
            throw new RuntimeException("Failed to write response body", ex);
        }
        response.setContentLength(body.length);
    }
}
