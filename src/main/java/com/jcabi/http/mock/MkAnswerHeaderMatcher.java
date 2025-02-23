/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

/**
 * Matcher for checking {@link MkAnswer#headers()} contents.
 * @since 1.5
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = {"header", "matcher"})
final class MkAnswerHeaderMatcher extends TypeSafeMatcher<MkAnswer> {
    /**
     * The header to match.
     */
    private final transient String header;

    /**
     * The Matcher to use against the header.
     */
    private final transient Matcher<Iterable<? extends String>> matcher;

    /**
     * Ctor.
     * @param hdr The header to match
     * @param match The matcher to use for the header
     */
    MkAnswerHeaderMatcher(final String hdr,
        final Matcher<Iterable<? extends String>> match) {
        super();
        this.header = hdr;
        this.matcher = match;
    }

    @Override
    public void describeTo(final Description description) {
        this.matcher.describeTo(
            description.appendText("MkAnswer containing header ")
                .appendText(this.header)
                .appendText(" with matching value(s) of: ")
        );
    }

    @Override
    public boolean matchesSafely(final MkAnswer item) {
        return item.headers().containsKey(this.header)
            && this.matcher.matches(item.headers().get(this.header));
    }
}
