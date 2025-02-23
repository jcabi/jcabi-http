/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.wire;

import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
import java.net.HttpURLConnection;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Integration case for {@link TrustedWire}.
 *
 * @since 1.10.1
 */
final class TrustedWireITCase {

    /**
     * TrustedWire can ignore SSL verifications.
     * @param url URL with SSL problems
     * @throws Exception If something goes wrong inside
     */
    @ParameterizedTest
    @CsvSource({
        "https://expired.badssl.com/",
        "https://self-signed.badssl.com/",
        "https://untrusted-root.badssl.com/"
    })
    void ignoresSslCertProblems(final String url) throws Exception {
        new JdkRequest(url)
            .through(TrustedWire.class)
            .fetch()
            .as(RestResponse.class)
            .assertStatus(HttpURLConnection.HTTP_OK);
    }

}
