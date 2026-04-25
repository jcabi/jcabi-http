/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for HTTP method constants exposed by {@link Request}.
 * @since 2.0
 */
final class RequestMethodsTest {

    @Test
    void exposesTraceMethodConstant() throws Exception {
        MatcherAssert.assertThat(
            "TRACE constant must equal \"TRACE\"",
            RequestMethodsTest.constant("TRACE"),
            Matchers.equalTo("TRACE")
        );
    }

    @Test
    void exposesConnectMethodConstant() throws Exception {
        MatcherAssert.assertThat(
            "CONNECT constant must equal \"CONNECT\"",
            RequestMethodsTest.constant("CONNECT"),
            Matchers.equalTo("CONNECT")
        );
    }

    private static String constant(final String name) throws Exception {
        final Field field = Request.class.getField(name);
        final int mods = field.getModifiers();
        MatcherAssert.assertThat(
            String.format("%s must be public static final", name),
            Modifier.isPublic(mods)
                && Modifier.isStatic(mods)
                && Modifier.isFinal(mods),
            Matchers.is(true)
        );
        return (String) field.get(null);
    }
}
