# collaboration-proto
A simple prototype of collaborative app

## Module structure
### simple-collaboration-app
This acts like a web server that hosts the collaborative web app.

A client connected to this web app can send or receive data in real time.

### simple-collaboration-client
This acts like a browser that connect to the web app.

### simple-collaboration-server
Later on, this server is to host the shared data of collaborative web apps.  
When two `simple-collaboration-app`s connects to the same `simple-collaboration-server`, their data is synced.

## Run
From project root
* Run the web app: 
  * `mvn -pl simple-collaboration-app exec:java`
  * or specify a port `mvn -pl simple-collaboration-app exec:java -Dexec.args=7001`. Default port is 7000.

* Open multiple clients on different terminals:
  * `mvn -pl simple-collaboration-client exec:java`
  * or specify the port of the web app `mvn -pl simple-collaboration-client exec:java -Dexec.args=7001`
  * On some clients, enter "sub" to subscribe the client to the web app
   ( just like opening the browser tab to the web app)
  * One a client, enter "set" then enter a string. See how the subscribing clients are notified. 
## References
* [RoutingMetadata](https://github.com/rsocket/rsocket/blob/master/Extensions/Routing.md)
* [Support TLS](https://stackoverflow.com/questions/58944152/rsocket-not-working-when-secured-with-tls-server-java-lang-unsupportedoperatio)

## Question & future development
* How to properly close a communication without `java.io.IOException: An existing connection was forcibly closed by the remote host` and `ClosedChannelException`.
* Client can fire-and-forget to "unsubscribe" itself after being subscribed.
