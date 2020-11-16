package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Server {
    private static int PORT = 7070;
    private static LinkedList<String> valuesHistory = new LinkedList<>();

    private static List<RSocket> appsRSockets = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        if (args.length > 0) {
            PORT = Integer.valueOf(args[0]);
        }
        Disposable server = startServer();
        valuesHistory.add("Init data");

        System.in.read();
        stopServer(server);
    }

    static Disposable startServer() {
        SocketAcceptor socketAcceptor = (setup, sendingSocket) -> Mono.just(new RSocket() {
            @Override
            public Mono<Payload> requestResponse(Payload payload) {
                if ("sub".equals(payload.getMetadataUtf8())) {
                    System.out.println("App subscribed: " + payload.getDataUtf8());
                    appsRSockets.add(sendingSocket);
                    return Mono.just(DefaultPayload.create(valuesHistory.getLast(), "serverUpdates"));
                }
                return Mono.empty();
            }

            @Override
            public Mono<Void> fireAndForget(Payload payload) {
                switch (payload.getMetadataUtf8()) {
                    case "set":
                        set(payload.getDataUtf8(), sendingSocket);
                        break;
                    default:
                        System.out.println("Server received: " + payload.getDataUtf8());
                }
                return Mono.empty();
            }

            @Override
            public Flux<Payload> requestStream(Payload payload) {
                if (payload.getMetadataUtf8().equals("sub")) {
                    return streamHandler();
                }
                return Flux.empty();
            }
        });

        Disposable server = RSocketServer.create(socketAcceptor)
            .bind(TcpServerTransport.create("localhost", PORT))
            .subscribe();
        System.out.println("Collaboration server is running at port " + PORT);
        return server;
    }

    static void stopServer(Disposable server) {
        server.dispose();
    }

    private static Flux<Payload> streamHandler() {
        return Flux.fromStream(valuesHistory.stream().map(DefaultPayload::create));
    }

    private static void set(String dataUtf8, RSocket sendingSocket) {
        Change change = Change.fromJson(dataUtf8);
        if (valuesHistory.getLast().equals(change.getOldValue())) {
            valuesHistory.add(change.getValue());
        }
        System.out.println("Collaboration data updated: " + change.getValue());
        appsRSockets.stream().filter(appSocket -> !appSocket.equals(sendingSocket))
            .forEach(appSocket -> appSocket.fireAndForget(DefaultPayload.create(change.getValue(), "serverUpdates"))
                .block());
    }
}
