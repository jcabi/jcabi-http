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
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 * @since 0.9
 * @see <a href="http://tools.ietf.org/html/rfc5988">RFC 5988 "Web Linking"</a>
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
@SuppressWarnings("PMD.TooManyMethods")
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
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public Map<String, WebLinkingResponse.Link> links() throws IOException {
        final ConcurrentMap<String, WebLinkingResponse.Link> links =
            new ConcurrentHashMap<>(0);
        final Collection<String> headers =
            this.headers().get(WebLinkingResponse.HEADER);
        if (headers != null) {
            for (final String header : headers) {
                for (final String part : header.split(",")) {
                    final WebLinkingResponse.Link link =
                        new WebLinkingResponse.SimpleLink(part.trim());
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
     *
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

    /**
     * Implementation of a link.
     *
     * @since 1.0
     */
    @Immutable
    @EqualsAndHashCode
    private static final class SimpleLink implements WebLinkingResponse.Link {

        /**
         * Pattern to match link value.
         */
        @SuppressWarnings("PMD.UnusedPrivateField")
        private static final Pattern PTN = Pattern.compile(
            "<([^>]+)>\\s*;(.*)"
        );

        /**
         * URI encapsulated.
         */
        private final transient String addr;

        /**
         * Map of link params.
         */
        private final transient ArrayMap<String, String> params;

        /**
         * Public ctor (parser).
         * @param text Text to parse
         * @throws IOException If fails
         */
        SimpleLink(final String text) throws IOException {
            this(WebLinkingResponse.SimpleLink.parse(text));
        }

        /**
         * Secondary ctor.
         * @param matcher Matcher object.
         */
        private SimpleLink(final Matcher matcher) {
            this(
                matcher.group(1),
                WebLinkingResponse.SimpleLink.parseParameters(matcher.group(2))
            );
        }

        /**
         * Primary ctor.
         * @param address Address
         * @param parameters Parameters
         */
        private SimpleLink(final String address,
            final Map<String, String> parameters) {
            this.addr = address;
            this.params = new ArrayMap<>(parameters);
        }

        @Override
        public URI uri() {
            return URI.create(this.addr);
        }

        @Override
        public int size() {
            return this.params.size();
        }

        @Override
        public boolean isEmpty() {
            return this.params.isEmpty();
        }

        @Override
        public boolean containsKey(final Object key) {
            return this.params.containsKey(key);
        }

        @Override
        public boolean containsValue(final Object value) {
            return this.params.containsValue(value);
        }

        @Override
        public String get(final Object key) {
            return this.params.get(key);
        }

        @Override
        public String put(final String key, final String value) {
            throw new UnsupportedOperationException("#put()");
        }

        @Override
        public String remove(final Object key) {
            throw new UnsupportedOperationException("#remove()");
        }

        @Override
        public void putAll(final Map<? extends String, ? extends String> map) {
            throw new UnsupportedOperationException("#putAll()");
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException("#clear()");
        }

        @Override
        public Set<String> keySet() {
            return this.params.keySet();
        }

        @Override
        public Collection<String> values() {
            return this.params.values();
        }

        @Override
        public Set<Map.Entry<String, String>> entrySet() {
            return this.params.entrySet();
        }

        /**
         * Match link with regexp.
         * @param link A link.
         * @return Matcher object
         * @throws IOException If fails
         */
        private static Matcher parse(final String link) throws IOException {
            final Matcher matcher = SimpleLink.PTN.matcher(link);
            if (!matcher.matches()) {
                throw new IOException(
                    String.format(
                        "Link header value doesn't comply to RFC-5988: \"%s\"",
                        matcher
                    )
                );
            }
            return matcher;
        }

        /**
         * Parse parameter string to map.
         * @param param Result of regexp matching
         * @return Map with parameters
         */
        private static Map<String, String> parseParameters(final String param) {
            final ConcurrentMap<String, String> args =
                new ConcurrentHashMap<>(0);
            for (final String pair
                : param.trim().split("\\s*;\\s*")) {
                final String[] parts = pair.split("=");
                args.put(
                    parts[0].trim().toLowerCase(Locale.ENGLISH),
                    parts[1].trim().replaceAll("(^\"|\"$)", "")
                );
            }
            return args;
        }
    }

}
