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
package com.jcabi.http;

import com.jcabi.aspects.Immutable;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Immutable HTTP header.
 *
 * @since 0.10
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "left", "right" })
public final class ImmutableHeader implements Map.Entry<String, String> {

    /**
     * Key.
     */
    private final transient String left;

    /**
     * Value.
     */
    private final transient String right;

    /**
     * Public ctor.
     * @param key The name of it
     * @param value The value
     */
    public ImmutableHeader(final String key, final String value) {
        this.left = ImmutableHeader.normalize(key);
        this.right = value;
    }

    @Override
    public String getKey() {
        return this.left;
    }

    @Override
    public String getValue() {
        return this.right;
    }

    @Override
    public String setValue(final String value) {
        throw new UnsupportedOperationException("#setValue()");
    }

    /**
     * Normalize key.
     * @param key The key to normalize
     * @return Normalized key
     */
    @SuppressWarnings("PMD.ProhibitPublicStaticMethods")
    public static String normalize(final String key) {
        final char[] chars = key.toCharArray();
        chars[0] = ImmutableHeader.upper(chars[0]);
        for (int pos = 1; pos < chars.length; ++pos) {
            if (chars[pos - 1] == '-') {
                chars[pos] = ImmutableHeader.upper(chars[pos]);
            }
        }
        return new String(chars);
    }

    /**
     * Convert char to upper case, if required.
     * @param chr The char to convert
     * @return Upper-case char
     */
    private static char upper(final char chr) {
        final char upper;
        if (chr >= 'a' && chr <= 'z') {
            upper = (char) (chr - ('a' - 'A'));
        } else {
            upper = chr;
        }
        return upper;
    }

}
