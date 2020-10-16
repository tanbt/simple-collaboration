package com.tanbt;

import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketConnector;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.util.DefaultPayload;
import java.util.Scanner;
import reactor.core.publisher.Mono;

/**
 * The browser
 */
public class Client {

    private static int PORT = 7000;
    private static String clientId;
    private static RSocket socket;

    public static void main(String[] args) {
        if (args.length > 0) {
            PORT = Integer.valueOf(args[0]);
        }
        socket = RSocketConnector.create()
            .acceptor(
                SocketAcceptor.forFireAndForget(payload -> {
                    System.out.println("\n" + payload.getDataUtf8());
                    return Mono.empty();
                }))
            .connect(TcpClientTransport.create("localhost", PORT))
            .block();

        Scanner in = new Scanner(System.in);
        System.out.print("Please enter client id to subscribe [NO WHITE SPACES]: ");
        clientId = in.nextLine();
        subscribeWithConfirmation();

        String cmd = "";
        do {
            System.out.print("Enter a command [set, get, exit]: ");
            cmd = in.nextLine();
            switch (cmd) {
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

    private static void subscribeWithConfirmation() {
        socket.fireAndForget(DefaultPayload.create(clientId, "subscribe")).block();
    }

    private static void send(String newData) {
        socket.fireAndForget(DefaultPayload.create(newData, "set")).block();
    }

    private static void get() {
        socket.fireAndForget(DefaultPayload.create(clientId, "get")).block();
    }
}
