/**
 * Copyright (c) 2011-2017, jcabi.com
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

import com.jcabi.http.request.ApacheRequest;
import com.jcabi.http.request.JdkRequest;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.SneakyThrows;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Integration case for use of ssl context.
 * @author Joris Lambrechts (lambrechts.joris@gmail.com)
 * @version $Id$
 */
public class RequestSslContextITCase {

    /**
     * The https url to verify secure connections can be requested.
     */
    private static final String URL = "https://sha512.badssl.com/";
    /**
     * Custom error message to verify custom ssl context is used.
     */
    private static final String MESSAGE = "Trust no one";
    /**
     * Assertion error message if fetch should succeed.
     */
    private static final String FETCH_SHOULD_FAIL =
            "fetch() should have thrown IOException.";

    /**
     * Test if {@link JdkRequest} uses the provided {@link SSLContext}.
     */
    @Test
    public final void jdkRequest() {
        try {
            new JdkRequest(URL)
                    .sslcontext(this.createContext())
                    .fetch();
            Assert.fail(FETCH_SHOULD_FAIL);
        } catch (final IOException exception) {
            final String expectedMessage =
                    "Failed GET request to https://sha512.badssl.com/";
            Assert.assertEquals(expectedMessage, exception.getMessage());
            final Throwable rootCause = ExceptionUtils.getRootCause(exception);
            Assert.assertThat(
                    rootCause,
                    CoreMatchers.<Throwable>instanceOf(
                            CertificateException.class
                    )
            );
            Assert.assertEquals(MESSAGE, rootCause.getMessage());
        }
    }

    /**
     * Test if {@link ApacheRequest} uses the provided {@link SSLContext}.
     */
    @Test
    public final void apacheRequest() {
        try {
            new ApacheRequest(URL)
                    .sslcontext(this.createContext())
                    .fetch();
            Assert.fail(FETCH_SHOULD_FAIL);
        } catch (final IOException exception) {
            final String expectedMessage =
                    "java.security.cert.CertificateException: Trust no one";
            Assert.assertEquals(expectedMessage, exception.getMessage());
            final Throwable rootCause = ExceptionUtils.getRootCause(exception);
            Assert.assertThat(
                    rootCause,
                    CoreMatchers.<Throwable>instanceOf(
                            CertificateException.class
                    )
            );
            Assert.assertEquals(MESSAGE, rootCause.getMessage());
        }
    }

    /**
     * Create a custom SSL Context that trusts no certificates.
     * @return A new TLS {@link SSLContext}
     */
    @SneakyThrows
    private SSLContext createContext() {
        final SSLContext tls = SSLContext.getInstance("TLS");
        tls.init(
                null,
                new TrustManager[] {new NoTrustManager()},
                new SecureRandom()
        );
        return tls;
    }

    /**
     * A trust manager that trusts nothing.
     */
    private static class NoTrustManager implements X509TrustManager {
        @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
        @Override
        public void checkClientTrusted(
                final X509Certificate[] chain,
                final String authtype
        ) {
        }
        @Override
        public void checkServerTrusted(
                final X509Certificate[] chain,
                final String authtype
        ) throws CertificateException {
            throw new CertificateException(MESSAGE);
        }
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }

    }

}
