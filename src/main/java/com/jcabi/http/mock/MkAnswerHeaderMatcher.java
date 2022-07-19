/*
 * Copyright (c) 2011-2022, jcabi.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met: 1) Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following
 * disclaimer. 2) Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided
 * with the distribution. 3) Neither the name of the jcabi.com nor
 * the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
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
