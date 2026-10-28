/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2026 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.request;

import com.jcabi.http.RequestBody;
import com.jcabi.http.Response;
import com.jcabi.http.Wire;
import jakarta.ws.rs.core.HttpHeaders;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

/**
 * Test case for {@link MultipartFormBody}.
 *
 * @since 2.0
 */
final class MultipartFormBodyTest {

    @Test
    void keepsDefaultFilename() {
        MatcherAssert.assertThat(
            "the existing overload must keep its default filename",
            MultipartFormBodyTest.body().formParam("upload", "data").get(),
            Matchers.containsString(
                "name=\"upload\"; filename=\"binary\""
            )
        );
    }

    @Test
    void setsFilenameSeparatelyFromFieldName() {
        MatcherAssert.assertThat(
            "the filename must not replace the form field name",
            MultipartFormBodyTest.body()
                .formParam("attachment", "data", "report final.txt").get(),
            Matchers.containsString(
                "name=\"attachment\"; filename=\"report final.txt\""
            )
        );
    }

    @Test
    void keepsFilenamesOfAllParts() {
        MatcherAssert.assertThat(
            "each part must retain its own filename",
            MultipartFormBodyTest.body()
                .formParam("first", "one", "one.txt")
                .formParam("second", "two", "two.txt")
                .formParam("third", "three").get(),
            Matchers.allOf(
                Matchers.containsString(
                    "name=\"first\"; filename=\"one.txt\""
                ),
                Matchers.containsString(
                    "name=\"second\"; filename=\"two.txt\""
                ),
                Matchers.containsString(
                    "name=\"third\"; filename=\"binary\""
                )
            )
        );
    }

    @Test
    void escapesQuotesAndBackslashesInFilename() {
        MatcherAssert.assertThat(
            "special characters must remain inside the quoted filename",
            MultipartFormBodyTest.body()
                .formParam("document", "content", "a\"b\\c.txt").get(),
            Matchers.containsString("filename=\"a\\\"b\\\\c.txt\"")
        );
    }

    @Test
    void sendsBinaryContentWithoutModification() throws IOException {
        final ByteArrayOutputStream sent = new ByteArrayOutputStream();
        final byte[] data = {0, -1, 66};
        new BaseRequest(
            (request, home, method, headers, content, connect, read) -> {
                IOUtils.copy(content, sent);
                return Mockito.mock(Response.class);
            }, "https://example.com"
        ).header(
            HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=test"
        ).multipartBody()
            .formParam("file", data, "image.bin")
            .back().fetch();
        Assertions.assertEquals(
            String.join(
                new String(new char[] {'\r', '\n'}), "--test",
                "Content-Disposition: form-data; name=\"file\"; filename=\"image.bin\"",
                "Content-Type: application/octet-stream", "",
                new String(data, StandardCharsets.ISO_8859_1), "--test--"
            ),
            new String(sent.toByteArray(), StandardCharsets.ISO_8859_1),
            "the wire must receive the original bytes and the custom filename"
        );
    }

    @ParameterizedTest
    @ValueSource(chars = {'\r', '\n'})
    void rejectsLineBreaksInFilename(final char newline) {
        Assertions.assertThrows(
            IllegalArgumentException.class,
            () -> MultipartFormBodyTest.body()
                .formParam("file", "data", String.format("bad%cname.txt", newline)),
            "a filename must not introduce another header line"
        );
    }

    @Test
    void rejectsFilenameForNonMultipartBody() {
        Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> new BaseRequest(
                Mockito.mock(Wire.class), "https://example.com"
            ).body().formParam("file", "data", "file.txt"),
            "a non-multipart body must not silently discard the filename"
        );
    }

    private static RequestBody body() {
        return new BaseRequest(
            Mockito.mock(Wire.class), "https://example.com"
        ).header(
            HttpHeaders.CONTENT_TYPE, "multipart/form-data; boundary=test"
        ).multipartBody();
    }
}
