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
