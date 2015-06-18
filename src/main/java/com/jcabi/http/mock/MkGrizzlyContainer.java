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

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import com.sun.grizzly.http.embed.GrizzlyWebServer;
import lombok.EqualsAndHashCode;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.util.Collection;

/**
 * Implementation of {@link MkContainer} based on Grizzly Server.
 *
 * @author Yegor Bugayenko (yegor@tpc2.com)
 * @version $Id$
 * @since 0.10
 * @see MkContainer
 * @checkstyle TooManyMethods (200 lines)
 */
@SuppressWarnings("PMD.TooManyMethods")
@EqualsAndHashCode(of = { "adapter", "gws", "port" })
@Loggable(Loggable.DEBUG)
public class MkGrizzlyContainer implements MkContainer {

    /**
     * Grizzly adapter.
     */
    private final transient MkGrizzlyAdapter adapter =
        new MkGrizzlyAdapter();

    /**
     * Grizzly container.
     */
    private transient GrizzlyWebServer gws;

    /**
     * Port where it works.
     */
    private transient int port;

    @Override
    public MkContainer next(final MkAnswer answer) {
        return this.next(answer, new IsAnything<MkQuery>());
    }

    @Override
    public MkContainer next(final MkAnswer answer,
        final Matcher<MkQuery> condition) {
        return this.next(answer, condition, 1);
    }

    @Override
    public MkContainer next(final MkAnswer answer,
        final Matcher<MkQuery> condition, final int count) {
        this.adapter.next(answer, condition, count);
        return this;
    }

    @Override
    public MkQuery take() {
        return this.adapter.take();
    }

    @Override
    public MkQuery take(final Matcher<MkAnswer> matcher) {
        return this.adapter.take(matcher);
    }

    @Override
    public Collection<MkQuery> takeAll(final Matcher<MkAnswer> matcher) {
        return this.adapter.takeAll(matcher);
    }

    @Override
    public int queries() {
        return this.adapter.queries();
    }

    @Override
    public MkContainer start() throws IOException {
        return this.start(MkGrizzlyContainer.reserve());
    }

    @Override
    public MkContainer start(final int prt) throws IOException {
        if (this.port != 0) {
            throw new IllegalStateException(
                String.format(
                    "already listening on port %d, use #stop() first",
                    this.port
                )
            );
        }
        this.port = prt;
        this.gws = new GrizzlyWebServer(this.port);
        this.gws.addGrizzlyAdapter(this.adapter, new String[] {"/"});
        this.gws.start();
        Logger.info(this, "started on port #%s", prt);
        return this;
    }

    @Override
    public void stop() {
        if (this.gws != null) {
            this.gws.stop();
        }
        Logger.info(this, "stopped on port #%s", this.port);
        this.port = 0;
    }

    @Override
    public URI home() {
        return URI.create(
            String.format("http://localhost:%d/", this.port)
        );
    }

    protected int getPort() {
        return this.port;
    }

    /**
     * Reserve port.
     * @return Reserved TCP port
     * @throws IOException If fails
     */
    private static int reserve() throws IOException {
        int reserved;
        final ServerSocket socket = new ServerSocket(0);
        try {
            reserved = socket.getLocalPort();
        } finally {
            socket.close();
        }
        return reserved;
    }

}
