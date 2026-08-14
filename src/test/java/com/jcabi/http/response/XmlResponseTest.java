/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.jcabi.http.request.FakeRequest;
import com.jcabi.xml.XML;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link XmlResponse}.
 * @since 1.1
 */
final class XmlResponseTest {

    /**
     * XmlResponse can find nodes with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void findsDocumentNodesWithXpath() throws Exception {
        MatcherAssert.assertThat(
            "should list both text nodes in order",
            new XmlResponse(
                new FakeRequest()
                    .withBody("<r><a>ура!</a><a>B</a></r>")
                    .fetch()
            ).xml().xpath("//a/text()"),
            Matchers.contains("ура!", "B")
        );
    }

    /**
     * XmlResponse can assert with XPath.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void assertsWithXpath() throws Exception {
        final XmlResponse response = new XmlResponse(
            new FakeRequest()
                .withBody("<x a='1'><!-- hi --><y>ура!</y></x>")
                .fetch()
        );
        Assertions.assertAll(
            () -> response.assertXPath("//y[.='ура!']"),
            () -> response.assertXPath("/x/@a"),
            () -> response.assertXPath("/x/comment()"),
            () -> response.assertXPath("/x/y[contains(.,'а')]")
        );
    }

    /**
     * XmlResponse can assert with XPath and namespaces.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void assertsWithXpathAndNamespaces() throws Exception {
        final XmlResponse response = new XmlResponse(
            new FakeRequest().withBody(
                StringUtils.join(
                    "<html xmlns='http://www.w3.org/1999/xhtml'>",
                    "<div>ура!</div></html>"
                )
            ).fetch()
        );
        Assertions.assertAll(
            () -> response.assertXPath("/xhtml:html/xhtml:div"),
            () -> response.assertXPath("//xhtml:div[.='ура!']")
        );
    }

    /**
     * XmlResponse can assert with XPath with custom namespaces.
     * @throws Exception If something goes wrong inside
     */
    @Test
    void assertsWithXpathWithCustomNamespace() throws Exception {
        final XML xml = new XmlResponse(
            new FakeRequest()
                .withBody("<a xmlns='urn:foo'><b>yes!</b></a>")
                .fetch()
        ).registerNs("foo", "urn:foo").xml();
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should be equal to 'yes!'",
                xml.xpath("//foo:b/text()").get(0),
                Matchers.equalTo("yes!")
            ),
            () -> MatcherAssert.assertThat(
                "should not be empty",
                xml.nodes("/foo:a/foo:b"),
                Matchers.not(Matchers.empty())
            )
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
        Assertions.assertAll(
            () -> MatcherAssert.assertThat(
                "should be equal 2",
                response.xml().nodes("//a"),
                Matchers.hasSize(2)
            ),
            () -> MatcherAssert.assertThat(
                "should be equal 1",
                response.xml().nodes("/root/a").get(0).xpath("x/text()").get(0),
                Matchers.equalTo("1")
            )
        );
    }
}
