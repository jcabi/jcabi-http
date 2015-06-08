/**
 * Copyright (c) 2011-2015, jcabi.com
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

import com.jcabi.manifests.Manifests;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * This file contains constant definitions that are used within this project.
 *
 * Standard {@link Charset Charsets} defined here are guaranteed to be
 * available on every implementation of the Java platform @since 1.7
 * @see <a href="Charset#standard">Standard Charsets</a>
 *
 * @author Simon Njenga (simtuje@gmail.com)
 * @version $Id$
 * @since 0.10
 * @see <a href="http://http.jcabi.com">http://http.jcabi.com/</a>
 */
public final class JcabiHttp {

    /**
     * An empty immutable {@code byte} array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * The Charset to use.
     */
    public static final Charset CHARSET = StandardCharsets.UTF_8;

    /**
     * The encoding to use.
     */
    public static final String ENCODING = CHARSET.name();

    /**
     * GET method name.
     */
    public static final String GET = "GET";

    /**
     * POST method name.
     */
    public static final String POST = "POST";

    /**
     * PUT method name.
     */
    public static final String PUT = "PUT";

    /**
     * HEAD method name.
     */
    public static final String HEAD = "HEAD";

    /**
     * DELETE method name.
     */
    public static final String DELETE = "DELETE";

    /**
     * OPTIONS method name.
     */
    public static final String OPTIONS = "OPTIONS";

    /**
     * PATCH method name.
     */
    public static final String PATCH = "PATCH";

    /**
     * UTF-8 error marker.
     */
    public static final String ERR = "\uFFFD";

    /**
     * Default user agent.
     */
    public static final String AGENT = String.format(
        "jcabi-%s/%s Java/%s",
        Manifests.read("JCabi-Version"),
        Manifests.read("JCabi-Build"),
        System.getProperty("java.version")
    );

    /**
     * Private constructor.
     */
    private JcabiHttp() {
        throw new IllegalStateException("Utility class - cannot instantiate!");
    }
}
