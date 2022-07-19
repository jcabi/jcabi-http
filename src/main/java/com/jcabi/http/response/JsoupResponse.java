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
package com.jcabi.http.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import lombok.EqualsAndHashCode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;

/**
 * Jsoup response.
 *
 * <p>This response decorator is able to parse HTTP response body as an HTML
 * document. Example usage:
 *
 * <pre> String body = new JdkRequest("http://my.example.com")
 *   .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
 *   .fetch()
 *   .as(JsoupResponse.class)
 *   .body();</pre>
 *
 * <p>{@link #body()} will try to output clean HTML even for
 *  malformed responses. For example:
 * <ul>
 *  <li>Unclosed tags will be closed ("&lt;p&gt;Hello" will become
 *      "&lt;p&gt;Hello&lt;/p&gt;")
 *  <li>Implicit tags will be made explicit (e.g. a naked &lt;td&gt; will be
 *      wrapped in a &lt;table&gt;&lt;tr&gt;&lt;td&gt;)
 *  <li>Basic structure is guaranteed (i.e. html, head, body elements)
 * </ul>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 1.4
 * @see <a href="http://jsoup.org/">Jsoup website</a>
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class JsoupResponse extends AbstractResponse {

    /**
     * Public ctor.
     * @param resp Response
     */
    public JsoupResponse(final Response resp) {
        super(resp);
    }

    @Override
    public String body() {
        final Document html = Jsoup.parse(super.body());
        html.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
        html.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
        return html.html();
    }

}
