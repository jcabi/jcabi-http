<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />

[![EO principles respected here](https://www.elegantobjects.org/badge.svg)](https://www.elegantobjects.org)
[![DevOps By Rultor.com](http://www.rultor.com/b/jcabi/jcabi-http)](http://www.rultor.com/p/jcabi/jcabi-http)
[![We recommend IntelliJ IDEA](https://www.elegantobjects.org/intellij-idea.svg)](https://www.jetbrains.com/idea/)

[![mvn](https://github.com/jcabi/jcabi-http/actions/workflows/mvn.yml/badge.svg)](https://github.com/jcabi/jcabi-http/actions/workflows/mvn.yml)
[![PDD status](http://www.0pdd.com/svg?name=jcabi/jcabi-http)](http://www.0pdd.com/p?name=jcabi/jcabi-http)
[![Maintainability](https://api.codeclimate.com/v1/badges/742bde48ea6fabdba1ce/maintainability)](https://codeclimate.com/github/jcabi/jcabi-http/maintainability)
[![Maven Central](https://img.shields.io/maven-central/v/com.jcabi/jcabi-http.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-http)
[![Javadoc](http://www.javadoc.io/badge/com.jcabi/jcabi-http.svg)](http://www.javadoc.io/doc/com.jcabi/jcabi-http)

[![codecov](https://codecov.io/gh/jcabi/jcabi-http/branch/master/graph/badge.svg)](https://codecov.io/gh/jcabi/jcabi-http)
[![Hits-of-Code](https://hitsofcode.com/github/jcabi/jcabi-http)](https://hitsofcode.com/view/github/jcabi/jcabi-http)
![Lines of code](https://img.shields.io/tokei/lines/github/jcabi/jcabi-http)
[![License](https://img.shields.io/badge/license-MIT-green.svg)](https://github.com/jcabi/jcabi-http/blob/master/LICENSE.txt)

More details are here: [http.jcabi.com](https://http.jcabi.com/index.html).

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

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```

Make sure you're using Maven 3.6+ and Java 8+.
