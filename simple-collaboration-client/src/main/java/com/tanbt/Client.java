package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.io.IOException;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * The browser
 */
public class Client {

    private static int PORT = 7000;
    private static final String clientId = UUID.randomUUID().toString();
    private static RSocket socket;

    public static void main(String[] args) throws IOException {
        socket = RSocketConnector.connectWith(
            TcpClientTransport.create("localhost", PORT)
        ).block();

        //sayHello();
        //subscribeStream();
        subscribe();

        System.in.read();
    }

    private static void sayHello() {
        socket
            .requestResponse(DefaultPayload.create(clientId, "hello"))
            .map(Payload::getDataUtf8)
            .doOnNext(System.out::println)
            .block();
    }

    private static void subscribeStream() {
        socket.requestStream(DefaultPayload.create(clientId, "subscribe"))
            .map(Payload::getDataUtf8)
            .doOnNext(System.out::println)
            .subscribe();
    }

    public static void subscribe() {
        RSocket rsocket =
            RSocketConnector.create()
                .acceptor(
                    SocketAcceptor.forFireAndForget(payload -> {
                        System.out.println(payload.getDataUtf8());
                        return Mono.empty();
                    }))
                .connect(TcpClientTransport.create("localhost", 7000))
                .block();
        rsocket.fireAndForget(DefaultPayload.create(clientId, "subscribe")).block();
    }
}
