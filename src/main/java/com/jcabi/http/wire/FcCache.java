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
package com.jcabi.http.wire;

import com.google.common.base.Joiner;
import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import com.jcabi.http.request.DefaultResponse;
import com.jcabi.immutable.Array;
import com.jcabi.log.Logger;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.commons.io.FileUtils;

/**
 * Cache for FcWire.
 *
 * @since 1.16
 */
@Immutable
@ToString
@EqualsAndHashCode
@SuppressWarnings("PMD.ExcessiveImports")
final class FcCache {

    /**
     * Body key.
     */
    private static final String BODY = "body";

    /**
     * Status key.
     */
    private static final String STATUS = "status";

    /**
     * Reason key.
     */
    private static final String REASON = "reason";

    /**
     * Headers key.
     */
    private static final String HEADERS = "headers";

    /**
     * Directory to keep files in.
     */
    private final transient String dir;

    /**
     * Ctor.
     */
    FcCache() {
        this(
            new File(
                new File(System.getProperty("java.io.tmpdir")),
                String.format(
                    "%s-%d",
                    FcCache.class.getCanonicalName(),
                    System.nanoTime()
                )
            ).getAbsolutePath()
        );
    }

    /**
     * Ctor.
     * @param path Dir with files
     */
    FcCache(final String path) {
        this.dir = path;
    }

    /**
     * Invalidate all.
     * @throws IOException If fails
     */
    public void invalidate() throws IOException {
        final File file = this.file("").getParentFile();
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
            Logger.debug(this, "cache invalidated in %s", file);
        }
    }

    /**
     * Get and cache.
     * @param label Label to use
     * @param wire Original wire
     * @param request The request
     * @param home URI to fetch
     * @param method HTTP method
     * @param headers Headers
     * @param input Input body
     * @param connect Connect timeout
     * @param read Read timeout
     * @return Response
     * @throws IOException If fails
     * @checkstyle ParameterNumberCheck (10 lines)
     */
    public Response get(final String label, final Wire wire,
        final Request request, final String home, final String method,
        final Collection<Map.Entry<String, String>> headers,
        final InputStream input, final int connect, final int read)
        throws IOException {
        final File file = this.file(label);
        final Response rsp;
        if (file.exists()) {
            rsp = this.response(request, file);
        } else {
            rsp = this.saved(
                wire.send(
                    request, home, method,
                    headers, input, connect, read
                ),
                file
            );
        }
        return rsp;
    }

    /**
     * Get response from file.
     * @param req Request
     * @param file File to read
     * @return Response
     * @throws IOException If fails
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private Response response(final Request req, final File file)
        throws IOException {
        final JsonObject json = Json.createReader(
            new ByteArrayInputStream(
                FileUtils.readFileToByteArray(file)
            )
        ).readObject();
        final List<Map.Entry<String, String>> map = new LinkedList<>();
        final JsonObject headers = json.getJsonObject(FcCache.HEADERS);
        for (final String name : headers.keySet()) {
            for (final JsonString value
                : headers.getJsonArray(name).getValuesAs(JsonString.class)) {
                map.add(new AbstractMap.SimpleEntry<>(name, value.getString()));
            }
        }
        Logger.debug(this, "cache loaded from %s", file);
        return new DefaultResponse(
            req,
            json.getInt(FcCache.STATUS),
            json.getString(FcCache.REASON),
            new Array<>(map),
            json.getString(FcCache.BODY).getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Save response to file.
     * @param response Response to save
     * @param file File to read
     * @return Response
     * @throws IOException If fails
     */
    private Response saved(final Response response, final File file)
        throws IOException {
        final JsonObjectBuilder json = Json.createObjectBuilder();
        json.add(FcCache.STATUS, response.status());
        json.add(FcCache.REASON, response.reason());
        final JsonObjectBuilder headers = Json.createObjectBuilder();
        for (final Map.Entry<String, List<String>> pair
            : response.headers().entrySet()) {
            final JsonArrayBuilder array = Json.createArrayBuilder();
            for (final String value : pair.getValue()) {
                array.add(value);
            }
            headers.add(pair.getKey(), array);
        }
        json.add(FcCache.HEADERS, headers);
        json.add(FcCache.BODY, response.body());
        if (file.getParentFile().mkdirs()) {
            Logger.debug(this, "directory created for %s", file);
        }
        try (OutputStream out = Files.newOutputStream(file.toPath())) {
            Json.createWriter(out).write(json.build());
        }
        Logger.debug(this, "cache saved into %s", file);
        return response;
    }

    /**
     * Make file from label.
     * @param label Label to use
     * @return File
     */
    private File file(final String label) {
        final String path;
        try {
            path = Joiner.on("/").join(
                URLEncoder.encode(label, StandardCharsets.UTF_8.toString())
                    .replaceAll("_", "__")
                    .replaceAll("\\+", "_")
                    .replaceAll("%", "_")
                    .split("(?<=\\G.{4})")
            );
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException(ex);
        }
        return new File(this.dir, String.format("%s.json", path));
    }

}
