/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
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
 * @since 1.1
 * @checkstyle ClassDataAbstractionCoupling (500 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
final class XmlResponseTest {

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
            "should be equal 2",
            response.xml().xpath("//a/text()"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            "should contains '\u0443\u0440\u0430'",
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
            "should be equal to 'yes!'",
            xml.xpath("//foo:b/text()").get(0),
            Matchers.equalTo("yes!")
        );
        MatcherAssert.assertThat(
            "should be empty",
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
            "should be equal 2",
            response.xml().nodes("//a"),
            Matchers.hasSize(2)
        );
        MatcherAssert.assertThat(
            "should be equal 1",
            response.xml().nodes("/root/a").get(0).xpath("x/text()").get(0),
            Matchers.equalTo("1")
        );
    }

}
