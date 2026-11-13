These PKCS12 stores contain public test-only private keys, with password
changeit. They are used exclusively by a loopback HTTPS server.

All leaf certificates have CN=localhost and SAN DNS:localhost, IP:127.0.0.1.
self-signed.p12 has a self-signed leaf valid until 2126.
expired.p12 has a self-signed leaf valid on January 1, 2000 only.
untrusted-root.p12 has a leaf signed by a separate test CA, with a two-certificate
chain valid until 2126. This CA is not installed in any trust store.

The stores were generated with JDK keytool, RSA 2048, and SHA256withRSA.
For the expired leaf, -startdate '2000/01/01 00:00:00' -validity 1 was used.
For the other certificates, -validity 36500 was used.
The CA uses -ext bc:c. Its leaf was signed with -certreq and -gencert, then
imported together with the CA certificate using -importcert.

Maven must copy these stores without resource filtering.
