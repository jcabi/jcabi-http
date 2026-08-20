/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.google.common.base.Joiner;
import com.jcabi.aspects.Immutable;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import jakarta.json.Json;
import jakarta.json.JsonStructure;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

/**
 * Body of a request with a form that has attachments.
 * @since 1.17
 */
final class MultipartFormBody implements RequestBody {

    /**
     * Content encapsulated.
     */
    @Immutable.Array
    private final transient byte[] text;

    /**
     * Base request encapsulated.
     */
    private final transient BaseRequest owner;

    /**
     * Public ctor.
     * @param req Request
     * @param body Text to encapsulate
     */
    MultipartFormBody(final BaseRequest req, final byte[] body) {
        this.owner = req;
        this.text = body.clone();
    }

    @Override
    public String toString() {
        return new RequestBody.Printable(this.text).toString();
    }

    @Override
    public Request back() {
        return this.owner.body(this.text);
    }

    @Override
    public String get() {
        return new String(this.text, StandardCharsets.UTF_8);
    }

    @Override
    public RequestBody set(final String txt) {
        return this.set(txt.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public RequestBody set(final JsonStructure json) {
        final StringWriter writer = new StringWriter();
        Json.createWriter(writer).write(json);
        return this.set(writer.toString());
    }

    @Override
    public RequestBody set(final byte[] txt) {
        return new MultipartFormBody(this.owner, txt);
    }

    @Override
    public RequestBody formParam(final String name, final Object value) {
        final String boundary = this.boundary();
        final String dashes = "--";
        final byte[] old;
        if (Arrays.equals(
            Arrays.copyOfRange(
                this.text,
                Math.max(this.text.length - 2, 0),
                this.text.length
            ),
            dashes.getBytes(StandardCharsets.UTF_8)
        )) {
            old = Arrays.copyOf(this.text, this.text.length - 2);
        } else {
            old = String.format("%s%s", dashes, boundary)
                .getBytes(StandardCharsets.UTF_8);
        }
        final byte[] bytes;
        if (value instanceof byte[]) {
            bytes = (byte[]) value;
        } else {
            bytes = value.toString().getBytes(StandardCharsets.UTF_8);
        }
        return new MultipartFormBody(
            this.owner,
            new MultipartBodyBuilder()
                .appendLine(old).appendLine(
                    Joiner.on("; ").join(
                        "Content-Disposition: form-data",
                        String.format("name=\"%s\"", name),
                        "filename=\"binary\""
                    ).getBytes(StandardCharsets.UTF_8)
                ).appendLine(
                    "Content-Type: application/octet-stream"
                        .getBytes(StandardCharsets.UTF_8)
                )
                .appendLine(new byte[0])
                .appendLine(bytes).append(
                    String.format(
                        "%s%s%s", dashes, boundary, dashes
                    ).getBytes(StandardCharsets.UTF_8)
                )
                .asBytes()
        );
    }

    @Override
    public RequestBody formParams(final Map<String, String> params) {
        RequestBody body = this;
        for (final Map.Entry<String, String> param : params.entrySet()) {
            body = body.formParam(param.getKey(), param.getValue());
        }
        return body;
    }

    private String boundary() {
        for (final Map.Entry<String, String> hdr : this.owner.headers()) {
            if (hdr.getKey().equals(HttpHeaders.CONTENT_TYPE)
                && hdr.getValue().matches(".*;\\s*[bB]oundary=.*")) {
                return hdr.getValue()
                    .replaceFirst(".*;\\s*[bB]oundary=", "");
            }
        }
        throw new IllegalStateException(
            "Content-Type: multipart/form-data requires boundary"
        );
    }
}
