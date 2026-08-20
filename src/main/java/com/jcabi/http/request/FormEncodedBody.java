/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.aspects.Immutable;
import com.jcabi.aspects.Loggable;
import com.jcabi.http.Request;
import com.jcabi.http.RequestBody;
import jakarta.json.Json;
import jakarta.json.JsonStructure;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * Body of a request with a simple form.
 * (enctype application/x-www-form-urlencoded)
 * @since 1.17
 */
@Immutable
@EqualsAndHashCode(of = "text")
@Loggable(Loggable.DEBUG)
final class FormEncodedBody implements RequestBody {

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
    FormEncodedBody(final BaseRequest req, final byte[] body) {
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
        return new FormEncodedBody(this.owner, txt);
    }

    @Override
    public RequestBody formParam(final String name, final Object value) {
        final StringBuilder builder = new StringBuilder(this.get());
        if (!builder.toString().isEmpty()) {
            builder.append('&');
        }
        return new FormEncodedBody(
            this.owner,
            builder
                .append(name)
                .append('=').append(
                    URLEncoder.encode(
                        value.toString(),
                        StandardCharsets.UTF_8
                    )
                )
                .toString()
                .getBytes(StandardCharsets.UTF_8)
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
}
