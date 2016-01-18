/**
 * Copyright (c) 2011-2015, jcabi.com
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
package com.jcabi.http.wire;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Tv;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.immutable.Array;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * This is dummy javadoc that will be updated later.
 *
 * @author Ievgen Degtiarenko (ievgen.degtiarenko@gmail.com)
 * @version $Id$
 */
@Immutable
@ToString
@EqualsAndHashCode(of = { "origin"})
public final class ETagCachingWire implements Wire {

    /**
     * Loader.
     */
    private static final CacheLoader<Wire, Cache<String, Response>> LOADER =
        new CacheLoader<Wire, Cache<String, Response>>() {
            @Override
            public Cache<String, Response> load(final Wire key) {
                return CacheBuilder
                        .newBuilder()
                        .expireAfterAccess((long) Tv.FIVE, TimeUnit.MINUTES)
                        .build();
            }
        };

    /**
     * Cache.
     */
    private static final LoadingCache<Wire, Cache<String, Response>> CACHE =
            CacheBuilder.newBuilder().build(ETagCachingWire.LOADER);

    /**
     * Original wire.
     */
    private final transient Wire origin;

    /**
     * Public constructor.
     * @param wire Original wire
     */
    public ETagCachingWire(final Wire wire) {
        this.origin = wire;
    }

    // @checkstyle ParameterNumber (7 lines)
    @Override
    public Response send(
            final Request req,
            final String home,
            final String method,
            final Collection<Map.Entry<String, String>> headers,
            final InputStream content,
            final int connect,
            final int read
    ) throws IOException {
        Response resp = this.origin.send(
                req, home, method, headers, content, connect, read
        );
        final String respetag = this.findETagHeader(resp.headers());
        if (respetag != null
                && resp.status() != HttpURLConnection.HTTP_NOT_MODIFIED) {
            try {
                CACHE.get(this).put(respetag, resp);
            } catch (final ExecutionException ex) {
                throw new IllegalStateException(ex);
            }
        }
        final Map.Entry<String, String> etag =
                this.findIfNoneMatchHeader(headers);
        if (etag != null
                && etag.getValue() != null
                && resp.status() == HttpURLConnection.HTTP_NOT_MODIFIED) {
            try {
                resp = CACHE.get(this).get(
                        etag.getValue(),
                        new Callable<Response>() {
                            @Override
                            public Response call() throws Exception {
                                return ETagCachingWire.this.origin.send(
                                    req, home, method,
                                    ETagCachingWire.this.noIfNoneMatch(headers),
                                    content, connect, read
                                );
                            }
                        });
            } catch (final ExecutionException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return resp;
    }

    /**
     * Finds ETag header value.
     * @param headers To search
     * @return ETag header value
     */
    private String findETagHeader(final Map<String, List<String>> headers) {
        final List<String> etags = headers.get("ETag");
        final String ret;
        if (etags != null && etags.size() == 1) {
            ret = etags.get(0);
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Finds for If-None-Match header.
     * @param headers To search
     * @return If-None-Match header or {@code null} if not found
     */
    private Map.Entry<String, String> findIfNoneMatchHeader(
            final Collection<Map.Entry<String, String>> headers
    ) {
        Map.Entry<String, String> found = null;
        for (final Map.Entry<String, String> header : headers) {
            if ("If-None-Match".equals(header.getKey())) {
                found = header;
                break;
            }
        }
        return found;
    }

    /**
     * This creates copy of the headers without If-None-Match.
     * @param headers To copy
     * @return Copy without If-None-Match
     */
    private Collection<Map.Entry<String, String>> noIfNoneMatch(
            final Collection<Map.Entry<String, String>> headers
    ) {
        return new Array<>(headers).less(this.findIfNoneMatchHeader(headers));
    }
}
