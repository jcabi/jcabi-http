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
    private static final byte[] CRLF = new byte[]{13, 10};

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
