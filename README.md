<img src="http://img.jcabi.com/logo.png" width="200px" height="48px" />

More details are here: [http.jcabi.com](http://http.jcabi.com/index.html)

```java
import com.jcabi.http.Request;
import com.jcabi.http.request.ApacheRequest;
public class Main {
  public static void main(String[] args) {
    String html = new ApacheRequest("https://www.google.com")
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

You need just this dependency:

```xml
<dependency>
  <groupId>com.jcabi</groupId>
  <artifactId>jcabi-http</artifactId>
  <version>1.0</version>
</dependency>
```

## Questions?

If you have any questions about the framework, or something doesn't work as expected,
please [submit an issue here](https://github.com/jcabi/jcabi-http/issues/new).
If you want to discuss, please use our [Google Group](https://groups.google.com/forum/#!forum/jcabi).

## How to contribute?

Fork the repository, make changes, submit a pull request.
We promise to review your changes same day and apply to
the `master` branch, if they look correct.

Please run Maven build before submitting a pull request:

```
$ mvn clean install -Pqulice
```
