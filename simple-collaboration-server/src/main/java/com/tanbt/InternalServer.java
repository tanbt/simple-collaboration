package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class InternalServer {
    private final String host;
    private final int port;
    private List<RSocket> appsRSockets;
    private Disposable server;

    private LinkedList<String> valuesHistory;


    public InternalServer(String host, int port) {
        this.host = host;
        this.port = port;
        appsRSockets = new ArrayList<>();

        valuesHistory = new LinkedList<>();
        valuesHistory.add("Init data");
    }

    void start() {
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

        server = RSocketServer.create(socketAcceptor)
            .bind(TcpServerTransport.create(host, port))
            .block();
        System.out.println("Collaboration server is running at port " + port);
    }

    void stop() {
        server.dispose();
    }

    public List<RSocket> getAppsRSockets() {
        return new ArrayList<>(appsRSockets);
    }

    public LinkedList<String> getValuesHistory() {
        return new LinkedList<>(valuesHistory);
    }

    private Flux<Payload> streamHandler() {
        return Flux.fromStream(valuesHistory.stream().map(DefaultPayload::create));
    }

    private void set(String dataUtf8, RSocket sendingSocket) {
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
