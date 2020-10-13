package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.io.IOException;
import java.time.Duration;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The web server
 */
public class Server {

    private static int PORT = 7000;

    public static void main(String[] args) throws IOException {
        // Define how to server different kind of communication
        RSocket rSocket = new RSocket() {
            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                System.out.println(payload.getDataUtf8());
                return Mono.just(DefaultPayload.create("Hello from server."));
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
                System.out.println(payload.getDataUtf8());
                return Flux.interval(Duration.ofMillis(1000))
                    .map(aLong -> DefaultPayload.create("Update: " + aLong));
            }
        };

        Disposable server = RSocketServer.create(SocketAcceptor.with(rSocket))
            .bind(TcpServerTransport.create("localhost", PORT))
            .subscribe();
        System.out.println("Web server is running at port " + PORT);

        System.in.read();
        server.dispose();
    }
}
