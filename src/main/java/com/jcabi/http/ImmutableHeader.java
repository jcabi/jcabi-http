/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
