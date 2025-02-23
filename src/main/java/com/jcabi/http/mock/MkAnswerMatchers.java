/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import org.hamcrest.Matcher;

/**
 * Convenient set of matchers for {@link MkAnswer}.
 * @since 1.5
 */
@SuppressWarnings("PMD.ProhibitPublicStaticMethods")
public final class MkAnswerMatchers {
    /**
     * Private ctor.
     */
    private MkAnswerMatchers() {
        // Utility class - cannot instantiate.
    }

    /**
     * Matches the value of the MkAnswer's body against the given matcher.
     *
     * @param matcher The matcher to use.
     * @return Matcher for checking the body of MkAnswer
     */
    public static Matcher<MkAnswer> hasBody(final Matcher<String> matcher) {
        return new MkAnswerBodyMatcher(matcher);
    }

    /**
     * Matches the value of the MkAnswer's body bytes against the given
     * matcher.
     * @param matcher The matcher to use
     * @return Matcher for checking the body of MkAnswer
     */
    public static Matcher<MkAnswer> hasBodyBytes(
        final Matcher<byte[]> matcher) {
        return new MkAnswerBodyBytesMatcher(matcher);
    }

    /**
     * Matches the content of the MkAnswer's header against the given
     * matcher. Note that for a valid match to occur, the header entry must
     * exist <i>and</i> its value(s) must match the given matcher.
     *
     * @param header The header to check.
     * @param matcher The matcher to use.
     * @return Matcher for checking the body of MkAnswer
     */
    public static Matcher<MkAnswer> hasHeader(final String header,
        final Matcher<Iterable<? extends String>> matcher) {
        return new MkAnswerHeaderMatcher(header, matcher);
    }

}
