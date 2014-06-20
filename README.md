<img src="http://img.jcabi.com/logo-square.png" width="64px" height="64px" />
 
[![Build Status](https://travis-ci.org/jcabi/jcabi-http.svg?branch=master)](https://travis-ci.org/jcabi/jcabi-http)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-http/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.jcabi/jcabi-http)

More details are here: [http.jcabi.com](http://http.jcabi.com/index.html)

```java
import com.jcabi.http.Request;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.response.HttpResponse;
public class Main {
  public static void main(String[] args) {
    String html = new JdkRequest("https://www.google.com")
      .uri().path("/users").queryParam("id", 333).back()
      .method(Request.GET)
      .header(HttpHeaders.ACCEPT, MediaType.TEXT_HTML)
      .fetch()
      .as(HttpResponse.class)
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
