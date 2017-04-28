package com.jcabi.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Map.Entry;

public class MockWire implements Wire {

    static Wire mockDelegate;
    
    public MockWire(Wire wire) {
    }

    @Override
    public Response send(Request req, String home, String method, Collection<Entry<String, String>> headers,
            InputStream content, int connect, int read) throws IOException {
        return mockDelegate.send(req, home, method, headers, content, connect, read);
    }

}
