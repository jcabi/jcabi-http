package com.jcabi.http.response;

import com.jcabi.aspects.Immutable;
import com.jcabi.http.Response;
import lombok.EqualsAndHashCode;
import org.yaml.snakeyaml.Yaml;


/**
 * Yaml response.
 *
 *
 * <pre>static final class Configuration {
 *     public String name;
 * }</pre>
 *
 * <pre> String name = new JdkRequest("http://example.com/example.yml")
 *   .header(HttpHeaders.ACCEPT, "application/yml")
 *   .fetch()
 *   .loadAs(Configuration.class)
 *   .name;</pre>
 *
 * <p>The class is immutable and thread-safe.
 *
 * @author Pastor (viruszold@gmail.com)
 * @version $Id$
 * @see <a href="www.snakeyaml.org/">SnakeYaml website</a>
 */
@Immutable
@EqualsAndHashCode(callSuper = true)
public final class YmlResponse extends AbstractResponse {
    private final Yaml yaml = new Yaml();

    public YmlResponse(Response resp) {
        super(resp);
    }

    public <E> E loadAs(Class<? extends E> clazz) {
        return yaml.loadAs(super.body(), clazz);
    }
}
