package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.io.IOException;

/**
 * The browser
 */
public class Client {

    private static int PORT = 7000;
    private static RSocket socket;

    public static void main(String[] args) throws IOException {
        socket = RSocketConnector.connectWith(
            TcpClientTransport.create("localhost", PORT)
        ).block();

        sayHello();
        subscribe();

        System.in.read();
    }

    private static void sayHello() {
        socket.requestResponse(DefaultPayload.create("Hello from client"))
            .map(Payload::getDataUtf8)
            .doOnNext(System.out::println)
            .block();
    }

    private static void subscribe() {
        socket.requestStream(DefaultPayload.create("Request streaming"))
            .map(Payload::getDataUtf8)
            .doOnNext(System.out::println)
            .subscribe();
    }
}
