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
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.immutable.ArrayMap;
import com.jcabi.matchers.XhtmlMatchers;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import com.jcabi.xml.XPathContext;
import java.net.URI;
import java.util.Map;
import javax.xml.namespace.NamespaceContext;
import lombok.EqualsAndHashCode;

/**
 * XML response.
 *
 * <p>This response decorator is able to parse HTTP response body as
 * an XML document and manipulate with it afterwords, for example:
 *
 * <pre> String name = new JdkRequest("http://my.example.com")
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
 *   .fetch()
 *   .as(XmlResponse.class)
 *   .assertXPath("/user/name")
 *   .xml()
 *   .xpath("/user/name/text()")
 *   .get(0);</pre>
 *
 * <p>In <a href="http://en.wikipedia.org/wiki/HATEOAS">HATEOAS</a>
 * responses it is convenient to use this decorator's
 * method {@link #rel(String)}
 * in order to follow the link provided in {@code &lt;link&gt;} XML element,
 * for example:
 *
 * <pre> String data = new JdkRequest("http://my.example.com")
 *   .header(HttpHeaders.ACCEPT, MediaType.TEXT_XML)
 *   .fetch()
 *   .as(XmlResponse.class)
 *   .rel("/user/links/link[@rel='next']/@href")
 *   .fetch()
 *   .body();</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @since 0.8
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class XmlResponse extends AbstractResponse {

    /**
     * Map of namespaces.
     */
    private final transient ArrayMap<String, String> namespaces;

    /**
     * Public ctor.
     * @param resp Response
     */
    public XmlResponse(final Response resp) {
        this(resp, new ArrayMap<String, String>());
    }

    /**
     * Public ctor.
     * @param resp Response
     * @param map Map of namespaces
     */
    private XmlResponse(final Response resp,
        final ArrayMap<String, String> map) {
        super(resp);
        this.namespaces = map;
    }

    /**
     * Get XML body.
     * @return XML body
     */
    public XML xml() {
        return new XMLDocument(this.body()).merge(this.context());
    }

    /**
     * Register this new namespace.
     * @param prefix Prefix to use
     * @param uri Namespace URI
     * @return This object
     */
    public XmlResponse registerNs(final String prefix, final String uri) {
        return new XmlResponse(this, this.namespaces.with(prefix, uri));
    }

    /**
     * Verifies HTTP response body XHTML/XML content against XPath query,
     * and throws {@link AssertionError} in case of mismatch.
     * @param xpath Query to use
     * @return This object
     */
    public XmlResponse assertXPath(final String xpath) {
        final String body = this.body();
        if (!XhtmlMatchers.hasXPath(xpath, this.context()).matches(body)) {
            throw new AssertionError(
                String.format(
                    "XML doesn't contain required XPath '%s':%n%s",
                    xpath, body
                )
            );
        }
        return this;
    }

    /**
     * Follow XML link.
     * @param query XPath query to fetch new URI
     * @return New request
     */
    public Request rel(final String query) {
        this.assertXPath(query);
        return new RestResponse(this).jump(
            URI.create(this.xml().xpath(query).get(0))
        );
    }

    /**
     * Create XPath context.
     * @return Context
     */
    private NamespaceContext context() {
        XPathContext context = new XPathContext();
        for (final Map.Entry<String, String> entry
            : this.namespaces.entrySet()) {
            context = context.add(entry.getKey(), entry.getValue());
        }
        return context;
    }

}
