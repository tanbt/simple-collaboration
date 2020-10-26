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

### Actor model
Actor mode is used by the web app to managed its clients' subscriptions.

The root actor `RootActor` is initialized when the web app is started.

When a client subscribes to the web app, the root actor spawns a `SubscriberActor` for that client.
When a client sets a new value, `SubscriberActor` signals the change to `RootActor`.
The shared data is updated by `RootActor`, this actor is then signals the share data to all `SubscriberActor`.

## Run
From project root, run each command below in separate terminals:
* Run the collaboration server on the default port `7070`:

```mvn -pl simple-collaboration-server exec:java```

* Run two apps on port `7000` and `7001` that are both connected to the collaboration server 
on port `7070` in separate terminals:

```
mvn -pl simple-collaboration-app exec:java -Dexec.args="7000 7070"
mvn -pl simple-collaboration-app exec:java -Dexec.args="7001 7070"
```

* Open multiple clients on different terminals:
For example, two clients connect to app "7000" and one connects to app "7001":
```
# name: client-one
mvn -pl simple-collaboration-client exec:java -Dexec.args=7000

# name: client-two
mvn -pl simple-collaboration-client exec:java -Dexec.args=7000

# name: client-three
mvn -pl simple-collaboration-client exec:java -Dexec.args=7001
```
when running each of the commands above, enter its client name.

From any client, enter "set", then input a string. See how the new data is propagated through each node. 
## References
* [RoutingMetadata](https://github.com/rsocket/rsocket/blob/master/Extensions/Routing.md)
* [Support TLS](https://stackoverflow.com/questions/58944152/rsocket-not-working-when-secured-with-tls-server-java-lang-unsupportedoperatio)

## Limitations
* Lots and lots of critical sections are not considered.

## Question & future development
* How to properly close a communication without `java.io.IOException: An existing connection was forcibly closed by the remote host` and `ClosedChannelException`.
* Client can fire-and-forget to "unsubscribe" itself after being subscribed.
* Remove a subscriber actor when its client socket is closed by the client. 
