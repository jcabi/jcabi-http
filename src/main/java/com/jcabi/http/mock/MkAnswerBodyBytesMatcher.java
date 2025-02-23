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
 * Matcher for checking {@link MkAnswer#bodyBytes()} result.
 * @since 0.17
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = "matcher")
final class MkAnswerBodyBytesMatcher extends TypeSafeMatcher<MkAnswer> {
    /**
     * The Matcher to use against the body.
     */
    private final transient Matcher<byte[]> matcher;

    /**
     * Ctor.
     * @param match The matcher to use for the body
     */
    MkAnswerBodyBytesMatcher(final Matcher<byte[]> match) {
        super();
        this.matcher = match;
    }

    @Override
    public void describeTo(final Description description) {
        this.matcher.describeTo(
            description.appendText("MkAnswer body bytes matching: ")
        );
    }

    @Override
    public boolean matchesSafely(final MkAnswer item) {
        return this.matcher.matches(item.bodyBytes());
    }
}
