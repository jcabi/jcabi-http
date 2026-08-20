/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.response;

import com.google.common.base.Splitter;
import com.jcabi.aspects.Immutable;
import com.jcabi.immutable.ArrayMap;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.EqualsAndHashCode;

/**
 * Implementation of a link.
 * @since 1.0
 */
@Immutable
@EqualsAndHashCode
final class SimpleLink implements WebLinkingResponse.Link {

    /**
     * Pattern to match link value.
     */
    private static final Pattern PTN = Pattern.compile(
        "<([^>]+)>\\s*;(.*)"
    );

    /**
     * Pattern to split parameters.
     */
    private static final Pattern SPLIT = Pattern.compile("\\s*;\\s*");

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
        this(SimpleLink.parse(text));
    }

    /**
     * Secondary ctor.
     * @param matcher Matcher object
     */
    private SimpleLink(final Matcher matcher) {
        this(
            matcher.group(1),
            SimpleLink.parseParameters(matcher.group(2))
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

    private static Map<String, String> parseParameters(final String param) {
        final ConcurrentMap<String, String> args =
            new ConcurrentHashMap<>(0);
        for (final String pair
            : Splitter.on(SimpleLink.SPLIT).split(param.trim())) {
            final List<String> parts =
                Splitter.on('=').limit(2).splitToList(pair);
            args.put(
                parts.get(0).trim().toLowerCase(Locale.ENGLISH),
                parts.get(1).trim().replaceAll("(^\"|\"$)", "")
            );
        }
        return args;
    }
}
