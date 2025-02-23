/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
 * @see <a href="http://jsoup.org/">Jsoup website</a>
 * @since 1.4
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
