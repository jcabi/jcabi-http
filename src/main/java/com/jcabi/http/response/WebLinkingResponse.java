/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.google.common.base.Splitter;
import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.EqualsAndHashCode;

/**
 * Web Linking response.
 *
 * <p>This response decorator is able to understand and parse {@code Link}
 * HTTP header according to
 * <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 "Web Linking"</a>,
 * for example:
 *
 * <pre> String name = new JdkRequest("http://my.example.com")
 *   .fetch()
 *   .as(WebLinkingResponse.class)
 *   .follow("next")
 *   .fetch();</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @see <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 "Web Linking"</a>
 * @since 0.9
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class WebLinkingResponse extends AbstractResponse {

    /**
     * ImmutableHeader name.
     */
    private static final String HEADER = "Link";

    /**
     * Param name.
     */
    private static final String REL = "rel";

    /**
     * Public ctor.
     * @param resp Response
     */
    public WebLinkingResponse(final Response resp) {
        super(resp);
    }

    /**
     * Follow link by REL.
     * @param rel Relation name
     * @return The same object
     * @throws IOException If fails
     */
    public Request follow(final String rel) throws IOException {
        final WebLinkingResponse.Link link = this.links().get(rel);
        if (link == null) {
            throw new IOException(
                String.format(
                    "Link with rel=\"%s\" doesn't exist, use #hasLink()",
                    rel
                )
            );
        }
        return new RestResponse(this).jump(link.uri());
    }

    /**
     * Get all links provided.
     * @return List of all links found
     * @throws IOException If fails
     */
    public Map<String, WebLinkingResponse.Link> links() throws IOException {
        final ConcurrentMap<String, WebLinkingResponse.Link> links =
            new ConcurrentHashMap<>(0);
        final Collection<String> headers =
            this.headers().get(WebLinkingResponse.HEADER);
        if (headers != null) {
            for (final String header : headers) {
                for (final String part : Splitter.on(',').split(header)) {
                    final WebLinkingResponse.Link link =
                        new SimpleLink(part.trim());
                    final String rel = link.get(WebLinkingResponse.REL);
                    if (rel != null) {
                        links.put(rel, link);
                    }
                }
            }
        }
        return links;
    }

    /**
     * Single link.
     * @since 1.0
     */
    @Immutable
    public interface Link extends Map<String, String> {

        /**
         * Its URI.
         * @return URI
         */
        URI uri();
    }
}
