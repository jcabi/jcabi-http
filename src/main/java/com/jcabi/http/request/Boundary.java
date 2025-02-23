/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.Random;

/**
 * Boundary for content-type multipart/form-data.
 * This is a copy of boundary created by Apache HttpComponents HttpClient 4.5.
 *
 * @since 1.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
public final class Boundary {
    /**
     * The pool of ASCII chars to be used for generating a multipart boundary.
     */
    private static final char[] MULTIPART_CHARS =
        "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .toCharArray();

    /**
     * Generation of pseudorandom numbers.
     */
    private final transient Random rand;

    /**
     * Constructor with new random number generation.
     */
    public Boundary() {
        this(new Random());
    }

    /**
     * Ctor.
     * @param random Random number generation.
     */
    public Boundary(final Random random) {
        this.rand = random;
    }

    /**
     * Generates random boundary with random size from 30 to 40.
     * @return Boundary value.
     */
    public String value() {
        final StringBuilder buffer = new StringBuilder();
        final int count = this.rand.nextInt(11) + 30;
        for (int index = 0; index < count; ++index) {
            buffer.append(
                Boundary.MULTIPART_CHARS[
                    this.rand.nextInt(MULTIPART_CHARS.length)
                    ]
            );
        }
        return buffer.toString();
    }
}
