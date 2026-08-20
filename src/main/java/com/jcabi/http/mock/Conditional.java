/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import org.hamcrest.Matcher;

/**
 * Answer with condition.
 * @since 1.5
 */
@EqualsAndHashCode(of = {"answr", "condition"})
final class Conditional {

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
     * @param ans The answer
     * @param matcher The matcher
     * @param times Number of times the answer should appear
     */
    Conditional(
        final MkAnswer ans, final Matcher<MkQuery> matcher,
        final int times
    ) {
        this(ans, matcher, Conditional.positiveAtomic(times));
    }

    /**
     * Ctor.
     * @param ans The answer
     * @param matcher The matcher
     * @param times Number of times the answer should appear
     */
    private Conditional(
        final MkAnswer ans, final Matcher<MkQuery> matcher,
        final AtomicInteger times
    ) {
        this.answr = ans;
        this.condition = matcher;
        this.count = times;
    }

    /**
     * Get the answer.
     * @return The answer
     */
    MkAnswer answer() {
        return this.answr;
    }

    /**
     * Does the query match the answer?
     * @param query The query to match
     * @return True, if the query matches the condition
     */
    boolean matches(final MkQuery query) {
        return this.condition.matches(query);
    }

    /**
     * Decrement the count for this conditional.
     * @return The updated count
     */
    int decrement() {
        return this.count.decrementAndGet();
    }

    private static AtomicInteger positiveAtomic(final int num) {
        if (num < 1) {
            throw new IllegalArgumentException(
                "Answer must be returned at least once."
            );
        }
        return new AtomicInteger(num);
    }
}
