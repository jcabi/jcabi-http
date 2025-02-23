/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import java.net.URI;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

/**
 * Convenient set of matchers for {@link MkQuery}.
 *
 * @since 1.5
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class MkQueryMatchers {

    /**
     * Private ctor.
     */
    private MkQueryMatchers() {
        // Utility class - cannot instantiate
    }

    /**
     * Matches the value of the MkQuery's body against the given matcher.
     *
     * @param matcher The matcher to use.
     * @return Matcher for checking the body of MkQuery
     */
    public static Matcher<MkQuery> hasBody(final Matcher<String> matcher) {
        return new MkQueryBodyMatcher(matcher);
    }

    /**
     * Matches the content of the MkQuery's header against the given matcher.
     * Note that for a valid match to occur, the header entry must exist
     * <i>and</i> its value(s) must match the given matcher.
     *
     * @param header The header to check.
     * @param matcher The matcher to use.
     * @return Matcher for checking the body of MkQuery
     */
    public static Matcher<MkQuery> hasHeader(
        final String header,
        final Matcher<Iterable<? extends String>> matcher
    ) {
        return new MkQueryHeaderMatcher(header, matcher);
    }

    /**
     * Matches the path of the MkQuery.
     *
     * @param path The path to check.
     * @return Matcher for checking the path of MkQuery
     */
    public static Matcher<MkQuery> hasPath(final Matcher<String> path) {
        return new MkQueryUriMatcher(
            Matchers.<URI>hasProperty("rawPath", path)
        );
    }

    /**
     * Matches the query of the MkQuery.
     *
     * @param query The query to check.
     * @return Matcher for checking the query of MkQuery
     */
    public static Matcher<MkQuery> hasQuery(final Matcher<String> query) {
        return new MkQueryUriMatcher(
            Matchers.<URI>hasProperty("rawQuery", query)
        );
    }

}
