/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import org.hamcrest.Matcher;

/**
 * Iterator over matching answers.
 * @since 1.17.3
 */
final class MkQueryIterator implements Iterator<MkQuery> {

    /**
     * Queue of results.
     */
    private final Queue<MkQuery> results;

    /**
     * Original iterator.
     */
    private final Iterator<QueryWithAnswer> iter;

    /**
     * Matcher.
     */
    private final Matcher<MkAnswer> matcher;

    /**
     * Ctor.
     * @param itr Original iterator
     * @param mtchr Matcher
     */
    MkQueryIterator(final Iterator<QueryWithAnswer> itr,
        final Matcher<MkAnswer> mtchr) {
        this.results = new ArrayDeque<>(0);
        this.iter = itr;
        this.matcher = mtchr;
    }

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
