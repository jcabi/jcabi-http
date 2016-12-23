<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![Made By Teamed.io](http://img.teamed.io/btn.svg)](http://www.teamed.io)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-http)](http://www.rultor.com/p/jcabi/jcabi-http)
[![We recommend IntelliJ IDEA](http://img.teamed.io/intellij-idea-recommend.svg)](https://www.jetbrains.com/idea/)

[![Build Status](https://travis-ci.org/jcabi/jcabi-http.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-http)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-http)](http://www.0pdd.com/p?name=jcabi/jcabi-http)
[![Build status](https://ci.appveyor.com/api/projects/status/a87rxtuvvek647fs/branch/master?svg=true)](https://ci.appveyor.com/project/yegor256/jcabi-http/branch/master)
[![Coverage Status](https://coveralls.io/repos/jcabi/jcabi-http/badge.svg?branch=__rultor&service=github)](https://coveralls.io/github/jcabi/jcabi-http?branch=__rultor)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.jcabi/jcabi-http/badge.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-http)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-http/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-http)
[![Dependencies](https://www.versioneye.com/user/projects/561a9e01a193340f32000eab/badge.svg?style=flat)](https://www.versioneye.com/user/projects/561a9e01a193340f32000eab)

More details are here: [http.jcabi.com](http://http.jcabi.com/index.html).
Also, read this blog post: [Fluent Java HTTP Client](http://www.yegor256.com/2014/04/11/jcabi-http-intro.html).

```java
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.RestResponse;
public class Main {
  public static void main(String[] args) {
    String html = new JdkRequest("https://www.google.com/test")
      .uri().path("/users").queryParam("id", 333).back()
      .method(Request.GET)
      .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
      .fetch()
      .as(RestResponse.class)
      .assertStatus(HttpURLConnection.HTTP_OK)
      .body();
  }
}
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-http/issues/new).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

Make sure you're using Maven 3.2+ and Java7+.
