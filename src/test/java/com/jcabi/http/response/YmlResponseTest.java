package com.jcabi.http.response;

import com.jcabi.http.Response;
import com.jcabi.http.request.FakeRequest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link YmlResponse}.
 */
public final class YmlResponseTest {
    static final class SimpleYml {
        public String source;
    }

    @Test
    public void loadAs() throws Exception {
        Response response = new FakeRequest()
                .withBody("source: empty").fetch();
        assertEquals(
                new YmlResponse(response).loadAs(SimpleYml.class).source,
                "empty");
    }

}