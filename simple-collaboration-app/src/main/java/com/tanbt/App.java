package com.tanbt;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * The web server
 */
public class App {

    private static int PORT = 7000;
    private static Map<String, RSocket> clientRSockets = new ConcurrentHashMap<>();
    private static String sharedData = "";

    public static void main(String[] args) throws IOException {

        SocketAcceptor socketAcceptor = (setup, sendingSocket) -> {
            return Mono.just(new RSocket() {
                @Override
                public Mono<Payload> requestResponse(Payload payload) {
                    switch (payload.getMetadataUtf8()) {
                        case "hello":
                            return helloRequestResponseHandler(payload);
                        default:
                            System.out.println("Server received: " + payload.getDataUtf8());
                            return Mono.empty();
                    }
                }

                @Override
                public Flux<Payload> requestStream(Payload payload) {
                    switch (payload.getMetadataUtf8()) {
                        case "subscribe":
                            return subscribeRequestStreamHandler();
                        default:
                            System.out.println("Server received: " + payload.getDataUtf8());
                            return Flux.empty();
                    }
                }

                @Override
                public Mono<Void> fireAndForget(Payload payload) {
                    switch (payload.getMetadataUtf8()) {
                        case "subscribe":
                            return subscribeFireAndForget(payload.getDataUtf8(), sendingSocket);
                        default:
                            System.out.println("Server received: " + payload.getDataUtf8());
                            return Mono.empty();
                    }
                }
            });
        };

        Disposable server = RSocketServer.create(socketAcceptor)
            .bind(TcpServerTransport.create("localhost", PORT))
            .subscribe();
        System.out.println("Web server is running at port " + PORT);

        new Thread(App::mockUpdater).start();

        System.in.read();
        server.dispose();
    }

    // Accidentally push data to subscribing clients after a delay
    private static void mockUpdater() {
        int i = 0;
        while (true) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sharedData = String.valueOf(++i);
            clientRSockets.entrySet().forEach(entry -> {
                new Thread(() -> {
                    entry.getValue().fireAndForget(DefaultPayload.create("Data: " + sharedData))
                        .doOnError(err -> {
                            System.out.println("Channel closed on client: " + entry.getKey());
                            clientRSockets.remove(entry.getKey());
                        }).block();
                }).start();
            });
        }
    }

    public static Mono<Payload> helloRequestResponseHandler(Payload payload) {
        return Mono.just(DefaultPayload.create("Hello client " + payload.getDataUtf8()));
    }

    public static Flux<Payload> subscribeRequestStreamHandler() {
        return Flux.interval(Duration.ofMillis(1000))
            .map(aLong -> DefaultPayload.create("Update: " + aLong));
    }

    public static Mono<Void> subscribeFireAndForget(String clientId, RSocket clientSocket) {
        clientRSockets.put(clientId, clientSocket);
        return Mono.empty();
    }
}
