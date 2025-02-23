/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.mock.MkAnswer;
import com.jcabi.http.mock.MkContainer;
import com.jcabi.http.mock.MkGrizzlyContainer;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.net.HttpURLConnection;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link TrustedWire}.
 * @since 1.10
 */
final class TrustedWireTest {

    /**
     * TrustedWire can ignore PKIX errors.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void ignoresPkixErrors() throws Exception {
        final MkContainer container = new MkGrizzlyContainer().next(
            new MkAnswer.Simple("")
        ).start();
        try {
            new JdkRequest(container.home())
                .through(TrustedWire.class)
                .fetch()
                .as(RestResponse.class)
                .assertStatus(HttpURLConnection.HTTP_OK);
        } finally {
            container.stop();
        }
    }

}
