/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import java.util.Arrays;

/**
 * Byte builder for multipart body.
 *
 * @since 1.0
 */
@Immutable
@Loggable(Loggable.DEBUG)
public final class MultipartBodyBuilder {
    /**
     * Carriage return constant.
     */
    private static final byte[] CRLF = {13, 10};

    /**
     * Byte array.
     */
    private final transient byte[] values;

    /**
     * Ctor.
     */
    public MultipartBodyBuilder() {
        this(new byte[0]);
    }

    /**
     * Ctor.
     * @param values Initial byte array.
     */
    public MultipartBodyBuilder(final byte[] values) {
        this.values = values.clone();
    }

    /**
     * Append byte array to this multipart body including carriage return.
     * @param bytes Byte array to append.
     * @return New multipart body.
     */
    public MultipartBodyBuilder appendLine(final byte[] bytes) {
        return this.append(bytes).append(MultipartBodyBuilder.CRLF);
    }

    /**
     * Bytes of multipart body.
     * @return Bytes array.
     */
    public byte[] asBytes() {
        return this.values.clone();
    }

    /**
     * Append byte array to this multipart body.
     * @param bytes Byte array to append.
     * @return New multipart body.
     */
    public MultipartBodyBuilder append(final byte[] bytes) {
        final int offset = this.values.length;
        final byte[] neww = Arrays.copyOf(this.values, offset + bytes.length);
        System.arraycopy(bytes, 0, neww, offset, bytes.length);
        return new MultipartBodyBuilder(neww);
    }
}
