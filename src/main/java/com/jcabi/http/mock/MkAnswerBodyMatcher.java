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
 * Matcher for checking {@link MkAnswer#body()} result.
 * @since 1.5
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = "matcher")
final class MkAnswerBodyMatcher extends TypeSafeMatcher<MkAnswer> {
    /**
     * The Matcher to use against the body.
     */
    private final transient Matcher<String> matcher;

    /**
     * Ctor.
     * @param match The matcher to use for the body
     */
    MkAnswerBodyMatcher(final Matcher<String> match) {
        super();
        this.matcher = match;
    }

    @Override
    public void describeTo(final Description description) {
        this.matcher.describeTo(
            description.appendText("MkAnswer body matching: ")
        );
    }

    @Override
    public boolean matchesSafely(final MkAnswer item) {
        return this.matcher.matches(item.body());
    }
}
