/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.http.Request;
import com.jcabi.immutable.Array;
import java.net.HttpURLConnection;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

/**
 * Test case for {@link DefaultResponse}.
 * @since 1.0
 */
final class DefaultResponseTest {

    /**
     * DefaultResponse can throw when entity is not a Unicode text.
     */
    @Test
    void throwsWhenEntityIsNotAUnicodeString() {
        Assertions.assertThrows(
            RuntimeException.class,
            new Executable() {
                @Override
                public void execute() {
                    new DefaultResponse(
                        Mockito.mock(Request.class),
                        HttpURLConnection.HTTP_OK,
                        "some text",
                        new Array<Map.Entry<String, String>>(),
                        // @checkstyle MagicNumber (1 line)
                        new byte[]{(byte) 0xC0, (byte) 0xC0}
                    ).body();
                }
            }
        );
    }

}
