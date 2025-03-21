/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import com.jcabi.log.Logger;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.hamcrest.Matcher;

/**
 * Mocker of Java Servlet container.
 *
 * @since 0.10
 * @checkstyle ClassDataAbstractionCouplingCheck (300 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class MkGrizzlyAdapter extends HttpHandler {

    /**
     * The encoding to use.
     */
    private static final String ENCODING = "UTF-8";

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

    // @checkstyle ExecutableStatementCount (55 lines)
    @Override
    @SuppressWarnings
        (
        {
        "PMD.AvoidCatchingThrowable",
        "PMD.AvoidInstantiatingObjectsInLoops",
        "rawtypes"
        }
        )
    public void service(
        final Request request,
        final Response response
    ) {
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
    public void next(
        final MkAnswer answer, final Matcher<MkQuery> query,
        final int count
    ) {
        this.conditionals.add(new Conditional(answer, query, count));
    }

    /**
     * Get the oldest request received.
     * @return Request received
     */
    public MkQuery take() {
        return this.queue.remove().que;
    }

    /**
     * Get the oldest request received subject to the matching condition.
     * ({@link java.util.NoSuchElementException} if no elements satisfy the
     * condition).
     * @param matcher The matcher specifying the condition
     * @return Request received satisfying the matcher
     */
    public MkQuery take(final Matcher<MkAnswer> matcher) {
        return this.takeMatching(matcher).next();
    }

    /**
     * Get the all requests received satisfying the given matcher.
     * ({@link java.util.NoSuchElementException} if no elements satisfy the
     * condition).
     * @param matcher The matcher specifying the condition
     * @return Collection of all requests satisfying the matcher, ordered from
     *  oldest to newest.
     */
    public Collection<MkQuery> takeAll(final Matcher<MkAnswer> matcher) {
        final Collection<MkQuery> results = new LinkedList<>();
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
    public int queries() {
        return this.queue.size();
    }

    /**
     * Get the all requests received satisfying the given matcher.
     * ({@link java.util.NoSuchElementException} if no elements satisfy the
     * condition).
     * @param matcher The matcher specifying the condition
     * @return Iterator over all requests
     */
    private Iterator<MkQuery> takeMatching(final Matcher<MkAnswer> matcher) {
        final Iterator<QueryWithAnswer> iter = this.queue.iterator();
        final Iterator<MkQuery> result = new MkQueryIterator(iter, matcher);
        if (!result.hasNext()) {
            throw new NoSuchElementException("No matching results found");
        }
        return result;
    }

    /**
     * Notify this response about failure.
     * @param response The response to notify
     * @param failure The failure just happened
     */
    private static void fail(
        final Response response,
        final Throwable failure
    ) {
        response.setStatus(HttpURLConnection.HTTP_INTERNAL_ERROR);
        try (PrintWriter writer = new PrintWriter(
            new OutputStreamWriter(
                response.createOutputStream(),
                MkGrizzlyAdapter.ENCODING
            )
        )
        ) {
            writer.print(Logger.format("%[exception]s", failure));
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private void handleRequest(final Request request, final Response response) throws IOException {
        final MkQuery query = new GrizzlyQuery(request);
        final boolean matched = this.processConditionals(query, response);
        if (!matched) {
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

    /**
     * Answer with condition.
     *
     * @since 1.5
     */
    @EqualsAndHashCode(of = {"answr", "condition"})
    private static final class Conditional {
        /**
         * The MkAnswer.
         */
        private final transient MkAnswer answr;

        /**
         * Condition for this answer.
         */
        private final transient Matcher<MkQuery> condition;

        /**
         * The number of times the answer is expected to appear.
         */
        private final transient AtomicInteger count;

        /**
         * Ctor.
         * @param ans The answer.
         * @param matcher The matcher.
         * @param times Number of times the answer should appear.
         */
        Conditional(
            final MkAnswer ans, final Matcher<MkQuery> matcher,
            final int times
        ) {
            this.answr = ans;
            this.condition = matcher;
            this.count = Conditional.positiveAtomic(times);
        }

        /**
         * Get the answer.
         * @return The answer
         */
        public MkAnswer answer() {
            return this.answr;
        }

        /**
         * Does the query match the answer?
         * @param query The query to match
         * @return True, if the query matches the condition
         */
        public boolean matches(final MkQuery query) {
            return this.condition.matches(query);
        }

        /**
         * Decrement the count for this conditional.
         * @return The updated count
         */
        public int decrement() {
            return this.count.decrementAndGet();
        }

        /**
         * Check if positive and convert to atomic.
         * @param num Number
         * @return Positive atomic integer
         */
        private static AtomicInteger positiveAtomic(final int num) {
            if (num < 1) {
                throw new IllegalArgumentException(
                    "Answer must be returned at least once."
                );
            }
            return new AtomicInteger(num);
        }

    }

    /**
     * Query with answer.
     *
     * @since 1.5
     */
    @EqualsAndHashCode(of = {"answr", "que"})
    private static final class QueryWithAnswer {
        /**
         * The answer.
         */
        private final transient MkAnswer answr;

        /**
         * The query.
         */
        private final transient MkQuery que;

        /**
         * Ctor.
         * @param qry The query
         * @param ans The answer
         */
        QueryWithAnswer(final MkQuery qry, final MkAnswer ans) {
            this.answr = ans;
            this.que = qry;
        }

        /**
         * Get the query.
         * @return The query.
         */
        public MkQuery query() {
            return this.que;
        }

        /**
         * Get the answer.
         * @return Answer
         */
        public MkAnswer answer() {
            return this.answr;
        }
    }

    /**
     * Iterator over matching answers.
     *
     * @since 1.17.3
     */
    @RequiredArgsConstructor
    private static final class MkQueryIterator implements Iterator<MkQuery> {

        /**
         * Queue of results.
         */
        private final Queue<MkQuery> results = new LinkedList<>();

        /**
         * Original iterator.
         */
        private final Iterator<QueryWithAnswer> iter;

        /**
         * Matcher.
         */
        private final Matcher<MkAnswer> matcher;

        @Override
        public boolean hasNext() {
            while (this.iter.hasNext()) {
                final QueryWithAnswer candidate = this.iter.next();
                if (this.matcher.matches(candidate.answer())) {
                    this.results.add(candidate.query());
                    this.iter.remove();
                    break;
                }
            }
            return !this.results.isEmpty();
        }

        @Override
        public MkQuery next() {
            if (this.results.isEmpty()) {
                throw new NoSuchElementException();
            }
            return this.results.remove();
        }

        @Override
        public void remove() {
            this.results.remove();
        }
    }
}
