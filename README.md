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
* Run `simple-collaboration-app`

## References
* [RoutingMetadata](https://github.com/rsocket/rsocket/blob/master/Extensions/Routing.md)
