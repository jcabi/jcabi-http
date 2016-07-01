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
package com.jcabi.http.mock;

    import java.net.HttpURLConnection;
    import java.util.Collections;
    import java.util.Map;
    import java.util.Set;
    import org.junit.Assert;
    import org.junit.Test;

/**
 * Test case for {@link MkAnswer.Simple}.
 * @author Alan Evans (thealanevans@gmail.com)
 * @version $Id$
 */
public final class MkAnswerSimpleTest {

    /**
     * MkAnswer.Simple can return the content as a clone.
     */
    @Test
    public void contentIsCloned() {
        final byte[] body = new byte[]{1, 2, 3};
        final MkAnswer answer = new MkAnswer.Simple(
            HttpURLConnection.HTTP_OK,
            this.getEmptyHeaders(),
            body
        );
        Assert.assertArrayEquals(body, answer.content());
        Assert.assertNotSame(body, answer.content());
        Assert.assertNotSame(answer.content(), answer.content());
    }

    /**
     * Gets a set of empty headers.
     * @return An empty header set.
     */
    private Set<Map.Entry<String, String>> getEmptyHeaders() {
        return Collections.<String, String>emptyMap().entrySet();
    }
}
