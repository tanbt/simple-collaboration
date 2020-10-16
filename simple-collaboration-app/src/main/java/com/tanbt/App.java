package com.tanbt;

import akka.actor.typed.ActorSystem;
import com.tanbt.protocol.CreateSubscriber;
import com.tanbt.protocol.MessageProtocol;
import com.tanbt.protocol.NotifySubscriber;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.SocketAcceptor;
import io.rsocket.core.RSocketServer;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import java.io.IOException;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

/**
 * The web server
 */
public class App {

    private static int PORT = 7000;
    private static String sharedData = "";

    private static ActorSystem<MessageProtocol> appRootActor;

    public static void main(String[] args) throws IOException {
        appRootActor = ActorSystem.create(RootActor.create(), "webapp");

        if (args.length > 0) {
            PORT = Integer.valueOf(args[0]);
        }

        // This socket acceptor is to handle client requests like what a web server does.
        // It doesn't mean to modify the app's state (shared data) directly,
        // but it forwards the request from a client to the subscriber actor which is handling that client.
        SocketAcceptor socketAcceptor = (setup, sendingSocket) -> {
            return Mono.just(new RSocket() {
                @Override
                public Mono<Payload> requestResponse(Payload payload) {
                    switch (payload.getMetadataUtf8()) {
                        case "subscribe":
                            return subscribe(payload, sendingSocket);
                        case "get":
                            return get();
                        default:
                            System.out.println("Server received: " + payload.getDataUtf8());
                            return Mono.empty();
                    }
                }

                @Override
                public Mono<Void> fireAndForget(Payload payload) {
                    switch (payload.getMetadataUtf8()) {
                        case "set":
                            set(payload.getDataUtf8());
                            break;
                        default:
                            System.out.println("Server received: " + payload.getDataUtf8());
                    }
                    return Mono.empty();
                }
            });
        };

        Disposable server = RSocketServer.create(socketAcceptor)
            .bind(TcpServerTransport.create("localhost", PORT))
            .subscribe();
        System.out.println("Web server is running at port " + PORT);

        System.in.read();
        server.dispose();
    }

    private static void set(String newData) {
        sharedData = newData;
        System.out.println("Shared data updated: " + sharedData);
        appRootActor.tell(new NotifySubscriber(newData));
    }

    public static Mono<Payload> get() {
        return Mono.just(DefaultPayload.create(sharedData));
    }

    public static Mono<Payload> subscribe(Payload payload, RSocket clientSocket) {
        appRootActor.tell(new CreateSubscriber(payload.getDataUtf8(), clientSocket));
        return Mono.just(DefaultPayload.create("Confirm client subscribed: " + payload.getDataUtf8()));
    }
}
