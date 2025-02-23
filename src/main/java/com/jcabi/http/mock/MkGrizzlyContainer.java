/*
 * SPDX-FileCopyrightText: Copyright (c) 2011-2025 Yegor Bugayenko
 * SPDX-License-Identifier: MIT
 */
package com.jcabi.http.mock;

import com.jcabi.aspects.Loggable;
import com.jcabi.log.Logger;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsAnything;

/**
 * Implementation of {@link MkContainer} based on Grizzly Server.
 * @see MkContainer
 * @since 0.10
 */
@SuppressWarnings("PMD.TooManyMethods")
@EqualsAndHashCode(of = {"adapter", "gws", "port"})
@Loggable(Loggable.DEBUG)
public final class MkGrizzlyContainer implements MkContainer {

    /**
     * Grizzly adapter.
     */
    private final transient MkGrizzlyAdapter adapter =
        new MkGrizzlyAdapter();

    /**
     * Grizzly container.
     */
    private transient HttpServer gws;

    /**
     * Port where it works.
     */
    private transient int port;

    @Override
    public MkContainer next(final MkAnswer answer) {
        return this.next(answer, new IsAnything<MkQuery>());
    }

    @Override
    public MkContainer next(
        final MkAnswer answer,
        final Matcher<MkQuery> condition
    ) {
        return this.next(answer, condition, 1);
    }

    @Override
    public MkContainer next(
        final MkAnswer answer,
        final Matcher<MkQuery> condition, final int count
    ) {
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
        return this.start(0);
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
        this.gws = new HttpServer();
        final NetworkListener listener = new NetworkListener(
            "grizzly",
            NetworkListener.DEFAULT_NETWORK_HOST,
            prt
        );
        this.gws.addListener(listener);
        this.gws.getServerConfiguration()
            .setAllowPayloadForUndefinedHttpMethods(true);
        this.gws.getServerConfiguration().addHttpHandler(
            this.adapter,
            "/"
        );
        this.gws.start();
        this.port = listener.getPort();
        Logger.info(this, "started on port #%s", this.port);
        return this;
    }

    @Override
    public void stop() {
        if (this.gws != null) {
            this.gws.shutdown();
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

    @Override
    public void close() {
        this.stop();
    }

}
