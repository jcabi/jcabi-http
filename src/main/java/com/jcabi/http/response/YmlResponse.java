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
package com.jcabi.http.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import lombok.EqualsAndHashCode;
import org.yaml.snakeyaml.Yaml;


/**
 * Yaml response.
 *
 * <pre>static final class Configuration {
 *     public String name;
 * }</pre>
 *
 * <pre> String name = new JdkRequest("http://example.com/example.yml")
 *   .header(HttpHeaders.ACCEPT, "application/yml")
 *   .fetch()
 *   .loadAs(Configuration.class)
 *   .name;</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Khlebnikov Andrey (viruszold@gmail.com)
 * @version $Id$
 * @see <a href="www.snakeyaml.org/">SnakeYaml website</a>
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class YmlResponse extends AbstractResponse {
    private final Yaml yaml = new Yaml();

    /**
     * Public ctor.
     * @param resp Response
     */
    public YmlResponse(final Response resp) {
        super(resp);
    }

    /**
     *
     * @param clazz Deserialized class
     * @return Deserialized object
     */
    public <E> E loadAs(final Class<? extends E> clazz) {
        return this.yaml.loadAs(super.body(), clazz);
    }
}
