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

import com.jcabi.http.Response;
import com.jcabi.http.request.FakeRequest;
import com.jcabi.xml.XML;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link XmlResponse}.
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 * @since 1.1
 */
@SuppressWarnings("PMD.TooManyMethods")
public final class XmlResponseTest {

    /**
     * XmlResponse can find nodes with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void findsDocumentNodesWithXpath() throws Exception {
        final XmlResponse response = new XmlResponse(
            new FakeRequest()
                .withBody("<r><a>\u0443\u0440\u0430!</a><a>B</a></r>")
                .fetch()
        );
        MatcherAssert.assertThat(
            response.xml().xpath("//a/text()"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            response.xml().xpath("/r/a/text()"),
            Matchers.hasItem("\u0443\u0440\u0430!")
        );
    }

    /**
     * XmlResponse can assert with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void assertsWithXpath() throws Exception {
        final Response resp = new FakeRequest()
            .withBody("<x a='1'><!-- hi --><y>\u0443\u0440\u0430!</y></x>")
            .fetch();
        new XmlResponse(resp)
            .assertXPath("//y[.='\u0443\u0440\u0430!']")
            .assertXPath("/x/@a")
            .assertXPath("/x/comment()")
            .assertXPath("/x/y[contains(.,'\u0430')]");
    }

    /**
     * XmlResponse can assert with XPath and namespaces.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void assertsWithXpathAndNamespaces() throws Exception {
        final Response resp = new FakeRequest().withBody(
            StringUtils.join(
                "<html xmlns='http://www.w3.org/1999/xhtml'>",
                "<div>\u0443\u0440\u0430!</div></html>"
            )
        ).fetch();
        new XmlResponse(resp)
            .assertXPath("/xhtml:html/xhtml:div")
            .assertXPath("//xhtml:div[.='\u0443\u0440\u0430!']");
    }

    /**
     * XmlResponse can assert with XPath with custom namespaces.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void assertsWithXpathWithCustomNamespace() throws Exception {
        final XmlResponse response = new XmlResponse(
            new FakeRequest()
                .withBody("<a xmlns='urn:foo'><b>yes!</b></a>")
                .fetch()
        ).registerNs("foo", "urn:foo");
        final XML xml = response.xml();
        MatcherAssert.assertThat(
            xml.xpath("//foo:b/text()").get(0),
            Matchers.equalTo("yes!")
        );
        MatcherAssert.assertThat(
            xml.nodes("/foo:a/foo:b"),
            Matchers.not(Matchers.empty())
        );
    }

    /**
     * XmlResponse can find and return nodes with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void findsDocumentNodesWithXpathAndReturnsThem() throws Exception {
        final XmlResponse response = new XmlResponse(
            new FakeRequest()
                .withBody("<root><a><x>1</x></a><a><x>2</x></a></root>")
                .fetch()
        );
        MatcherAssert.assertThat(
            response.xml().nodes("//a"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            response.xml().nodes("/root/a").get(0).xpath("x/text()").get(0),
            Matchers.equalTo("1")
        );
    }

}
