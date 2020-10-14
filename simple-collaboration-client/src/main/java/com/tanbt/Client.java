package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.util.Scanner;
import java.util.UUID;
import reactor.core.publisher.Mono;

/**
 * The browser
 */
public class Client {

    private static int PORT = 7000;
    private static final String clientId = UUID.randomUUID().toString();
    private static RSocket socket;

    public static void main(String[] args) {
        socket = RSocketConnector.create()
            .acceptor(
                SocketAcceptor.forFireAndForget(payload -> {
                    System.out.println("\n" + payload.getDataUtf8());
                    return Mono.empty();
                }))
            .connect(TcpClientTransport.create("localhost", PORT))
            .block();
        System.out.println("Client id: " + clientId);
        Scanner in = new Scanner(System.in);
        String cmd = "";
        do {
            System.out.print("Enter a command [sub, set, get, exit]: ");
            cmd = in.nextLine();
            switch (cmd) {
                case "sub":
                    subscribeWithConfirmation();
                    break;
                case "set":
                    System.out.print("Please enter new data: ");
                    String newData = in.nextLine();
                    send(newData);
                    break;
                case "get": {
                    get();
                    break;
                }
            }
        } while (!"exit".equals(cmd));
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

    private static void subscribeWithConfirmation() {
        socket.requestResponse(DefaultPayload.create(clientId, "subscribe"))
            .map(Payload::getDataUtf8).doOnNext(System.out::println).block();
    }

    private static void subscribe() {
        socket.fireAndForget(DefaultPayload.create(clientId, "subscribe")).block();
    }

    private static void send(String newData) {
        socket.fireAndForget(DefaultPayload.create(newData, "set")).block();
    }

    private static void get() {
        socket.requestResponse(DefaultPayload.create(clientId, "get"))
            .map(Payload::getDataUtf8).doOnNext(data -> System.out.println("Current data: " + data)).block();
    }
}
