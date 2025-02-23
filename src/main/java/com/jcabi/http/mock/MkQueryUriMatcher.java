/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import java.net.URI;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/**
 * Matcher for checking {@link MkQuery#uri()} contents.
 *
 * @since 1.17.4
 * @checkstyle ProtectedMethodInFinalClassCheck (50 lines)
 */
public final class MkQueryUriMatcher
    extends TypeSafeDiagnosingMatcher<MkQuery> {
    /**
     * Path to match.
     */
    private final transient Matcher<URI> matcher;

    /**
     * Constructor.
     *
     * @param mtrch Path to match.
     */
    MkQueryUriMatcher(final Matcher<URI> mtrch) {
        super();
        this.matcher = mtrch;
    }

    @Override
    public void describeTo(final Description desc) {
        desc.appendDescriptionOf(this.matcher);
    }

    @Override
    protected boolean matchesSafely(
        final MkQuery item, final Description desc
    ) {
        final URI uri = item.uri();
        desc.appendText("actual uri ").appendValue(uri);
        this.matcher.describeMismatch(uri, desc);
        return this.matcher.matches(uri);
    }
}
