/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import lombok.EqualsAndHashCode;

/**
 * Query with answer.
 * @since 1.5
 */
@EqualsAndHashCode(of = {"answr", "que"})
final class QueryWithAnswer {

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
     * @return The query
     */
    MkQuery query() {
        return this.que;
    }

    /**
     * Get the answer.
     * @return Answer
     */
    MkAnswer answer() {
        return this.answr;
    }
}
